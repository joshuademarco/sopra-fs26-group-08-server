package ch.uzh.ifi.hase.soprafs26.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.CalendarToken;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.CalendarTokenRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CalendarEventGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FreeSlotGetDTO;

@Service
@Transactional
public class CalendarService {

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_CALENDAR_EVENTS_URL = "https://www.googleapis.com/calendar/v3/calendars/primary/events";
    private static final String GOOGLE_FREEBUSY_URL = "https://www.googleapis.com/calendar/v3/freeBusy";
    private static final String GOOGLE_CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar.readonly";

    // Fixed issue of not mapping correct values
    // source:
    // https://developers.google.com/workspace/calendar/api/v3/reference/freebusy/query?_gl=1*9taivb*_up*MQ..*_ga*MjA0Nzg1OTIzMC4xNzc2NjI3NDI2*_ga_SM8HXJ53K2*czE3NzY2Mjc0MjYkbzEkZzAkdDE3NzY2Mjc0MjYkajYwJGwwJGgw
    @Value("${google.calendar.client-id:}")
    private String clientId;

    @Value("${google.calendar.client-secret:}")
    private String clientSecret;

    @Value("${google.calendar.redirect-uri}")
    private String redirectUri;

    @Value("${google.calendar.frontend-redirect-uri}")
    private String frontendRedirectUri;

    private final CalendarTokenRepository calendarTokenRepository;
    private final UserRepository userRepository;

    CalendarService(CalendarTokenRepository calendarTokenRepository, UserRepository userRepository) {
        this.calendarTokenRepository = calendarTokenRepository;
        this.userRepository = userRepository;
    }

    public String getConnectUrl(Long userId) {
        return GOOGLE_AUTH_URL
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode(GOOGLE_CALENDAR_SCOPE)
                + "&access_type=offline"
                + "&prompt=consent"
                + "&state=" + userId;
    }

    public void handleOAuthCallback(String code, Long userId) {
        String body = "code=" + encode(code)
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&redirect_uri=" + encode(redirectUri)
                + "&grant_type=authorization_code";

        JSONObject tokens = postToTokenEndpoint(body);

        String accessToken = tokens.getString("access_token");
        String refreshToken = tokens.optString("refresh_token", null);
        int expiresIn = tokens.optInt("expires_in", 3600);

        CalendarToken calendarToken = calendarTokenRepository.findByUserId(userId).orElseGet(CalendarToken::new);
        calendarToken.setUserId(userId);
        calendarToken.setAccessToken(accessToken);
        calendarToken.setExpiresAt(Instant.now().plusSeconds(expiresIn));

        if (refreshToken != null) {
            calendarToken.setRefreshToken(refreshToken);
        }

        calendarTokenRepository.save(calendarToken);
    }

    /** Returns true when the user has a stored Google Calendar token. */
    public boolean isConnected(Long userId) {
        return calendarTokenRepository.findByUserId(userId).isPresent();
    }

    /** Removes stored Google Calendar tokens for the user. */
    public void disconnect(Long userId) {
        calendarTokenRepository.findByUserId(userId).ifPresent(calendarTokenRepository::delete);
    }

    public List<CalendarEventGetDTO> getEvents(Long userId) {
        CalendarToken token = getValidToken(userId);

        String url = GOOGLE_CALENDAR_EVENTS_URL
                + "?timeMin=" + encode(Instant.now().toString())
                + "&maxResults=20"
                + "&orderBy=startTime"
                + "&singleEvents=true";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Google Calendar API error: " + response.statusCode());
            }

            return parseEvents(response.body());

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch calendar events");
        }
    }

    public List<FreeSlotGetDTO> findFreeSlots(List<Long> userIds, Instant from, Instant to) {
        if (userIds == null || userIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userIds must not be empty");
        }

        JSONArray items = new JSONArray();
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

            JSONObject item = new JSONObject();
            item.put("id", user.getEmail());
            items.put(item);
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("timeMin", from.toString());
        requestBody.put("timeMax", to.toString());
        requestBody.put("items", items);

        CalendarToken token = getValidToken(userIds.get(0));

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(GOOGLE_FREEBUSY_URL))
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Google FreeBusy API error: " + response.statusCode());
            }

            return computeFreeSlotsFromJson(response.body(), from, to);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to query free/busy data");
        }
    }

    public List<FreeSlotGetDTO> findGroupFreeSlots(List<Long> userIds, Instant from, Instant to) {
        List<Instant[]> allBusy = new ArrayList<>();
        for (Long userId : userIds) {
            allBusy.addAll(getBusyIntervalsForUser(userId, from, to));
        }
        allBusy.sort((a, b) -> a[0].compareTo(b[0]));
        return computeFreeSlots(allBusy, from, to);
    }

    public String getFrontendRedirectUri() {
        return frontendRedirectUri;
    }

    // #region Helpers

    private CalendarToken getValidToken(Long userId) {
        CalendarToken token = calendarTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Google Calendar not connected for this user"));

        // Refresh 60 seconds before expiry
        if (token.getExpiresAt() != null && Instant.now().isAfter(token.getExpiresAt().minusSeconds(60))) {
            token = refreshAccessToken(token);
        }
        return token;
    }

    private CalendarToken refreshAccessToken(CalendarToken token) {
        if (token.getRefreshToken() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "No refresh token stored. Please reconnect Google Calendar.");
        }

        String body = "grant_type=refresh_token"
                + "&refresh_token=" + encode(token.getRefreshToken())
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret);

        JSONObject response = postToTokenEndpoint(body);

        token.setAccessToken(response.getString("access_token"));
        token.setExpiresAt(Instant.now().plusSeconds(response.optInt("expires_in", 3600)));

        return calendarTokenRepository.save(token);
    }

    private JSONObject postToTokenEndpoint(String formBody) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(GOOGLE_TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Google token endpoint error: " + response.body());
            }
            return new JSONObject(response.body());

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Failed to get Google token endpoint");
        }
    }

    private List<CalendarEventGetDTO> parseEvents(String json) {
        JSONObject root = new JSONObject(json);
        JSONArray itemsArray = root.optJSONArray("items");

        List<CalendarEventGetDTO> events = new ArrayList<>();
        if (itemsArray == null)
            return events;

        for (int i = 0; i < itemsArray.length(); i++) {

            JSONObject item = itemsArray.getJSONObject(i);
            CalendarEventGetDTO dto = new CalendarEventGetDTO();

            dto.setId(item.optString("id", ""));
            dto.setSummary(item.optString("summary", "Empty"));
            dto.setDescription(item.optString("description", ""));
            dto.setLocation(item.optString("location", ""));

            JSONObject startObj = item.optJSONObject("start");
            JSONObject endObj = item.optJSONObject("end");

            if (startObj != null) {
                if (startObj.has("dateTime")) {
                    dto.setStart(startObj.getString("dateTime"));
                    dto.setAllDay(false);
                } else {
                    dto.setStart(startObj.optString("date", ""));
                    dto.setAllDay(true);
                }
            }
            if (endObj != null) {
                dto.setEnd(endObj.has("dateTime") ? endObj.getString("dateTime") : endObj.optString("date", ""));
            }

            events.add(dto);
        }
        return events;
    }

    private List<Instant[]> getBusyIntervalsForUser(Long userId, Instant from, Instant to) {
        if (calendarTokenRepository.findByUserId(userId).isEmpty()) {
            return new ArrayList<>();
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return new ArrayList<>();

        CalendarToken token = getValidToken(userId);

        JSONArray items = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("id", user.getEmail());
        items.put(item);

        JSONObject requestBody = new JSONObject();
        requestBody.put("timeMin", from.toString());
        requestBody.put("timeMax", to.toString());
        requestBody.put("items", items);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(GOOGLE_FREEBUSY_URL))
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200)
                return new ArrayList<>();

            return extractBusyIntervals(response.body());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Instant[]> extractBusyIntervals(String json) {
        JSONObject root = new JSONObject(json);
        JSONObject calendars = root.optJSONObject("calendars");
        List<Instant[]> intervals = new ArrayList<>();
        if (calendars == null)
            return intervals;
        for (String email : calendars.keySet()) {
            JSONArray busy = calendars.getJSONObject(email).optJSONArray("busy");
            if (busy == null)
                continue;
            for (int i = 0; i < busy.length(); i++) {
                JSONObject interval = busy.getJSONObject(i);
                intervals.add(new Instant[] {
                        Instant.parse(interval.getString("start")),
                        Instant.parse(interval.getString("end"))
                });
            }
        }
        return intervals;
    }

    /**
     * Maps the free time slots within [from, to] given a sorted list of busy
     * intervals.
     */
    private List<FreeSlotGetDTO> computeFreeSlots(List<Instant[]> busyIntervals, Instant from, Instant to) {
        List<FreeSlotGetDTO> freeSlots = new ArrayList<>();
        Instant cursor = from;

        for (Instant[] busy : busyIntervals) {
            if (busy[0].isAfter(cursor)) {
                FreeSlotGetDTO slot = new FreeSlotGetDTO();
                slot.setStart(cursor.toString());
                slot.setEnd(busy[0].toString());
                freeSlots.add(slot);
            }
            if (busy[1].isAfter(cursor)) {
                cursor = busy[1];
            }
        }

        if (cursor.isBefore(to)) {
            FreeSlotGetDTO slot = new FreeSlotGetDTO();
            slot.setStart(cursor.toString());
            slot.setEnd(to.toString());
            freeSlots.add(slot);
        }

        return freeSlots;
    }

    /**
     * Legacy: parses busy intervals from freeBusy JSON then delegates to
     * computeFreeSlots.
     */
    private List<FreeSlotGetDTO> computeFreeSlotsFromJson(String json, Instant from, Instant to) {
        List<Instant[]> busyIntervals = extractBusyIntervals(json);
        busyIntervals.sort((a, b) -> a[0].compareTo(b[0]));
        return computeFreeSlots(busyIntervals, from, to);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

// #endregion
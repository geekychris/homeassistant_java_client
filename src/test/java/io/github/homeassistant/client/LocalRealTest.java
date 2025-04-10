package io.github.homeassistant.client;

import io.github.homeassistant.auth.AuthenticationManager;
import io.github.homeassistant.model.Event;
import io.github.homeassistant.model.Service;
import io.github.homeassistant.model.State;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class LocalRealTest {

    private static final String BASE_URL = "http://localhost:8123";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiIzYmRmM2Y0YTczZDY0YzEwYTRiYjcwMzYxMTE0NzljNCIsImlhdCI6MTc0MzI3NjM5MCwiZXhwIjoyMDU4NjM2MzkwfQ.BoalZzAtx1t2Ko_E06h1dB9nl-V2Faw8YYvhB7Gvnx8";
    private static final String ENTITY_ID = "light.living_room";
    private static final String DOMAIN = "light";
    private static final String SERVICE = "turn_on";
    private static final String EVENT_TYPE = "custom_event";

    @Test
    public void d(){
            String dateString = "2025-03-25T04:50:56.076866+00:00";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSxxx");
            ZonedDateTime zdt = ZonedDateTime.parse(dateString, formatter);
            System.out.println(zdt);

    }

    @Test
    public void testA() throws AuthenticationManager.AuthenticationException, HomeAssistantException {
        // Rest of your method implementation...

        HomeAssistantClient hac = new HomeAssistantClient("http://homeassistant.local:8123", ACCESS_TOKEN, null);
        State state = hac.getState("binary_sensor.tz3000_gntwytxo_ts0203_opening");

        List<State> states = hac.getStates();
        //List<Event> events = hac.getEvents();
        Map<String, Map<String, Service>> services = hac.getServices();
        System.out.println();
    }
}

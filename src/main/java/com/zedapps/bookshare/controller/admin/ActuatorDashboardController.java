package com.zedapps.bookshare.controller.admin;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.enums.ActivityType;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

/**
 * @author smzoha
 * @since 6/4/26
 **/
@Controller
@RequestMapping("/admin/actuator")
@RequiredArgsConstructor
public class ActuatorDashboardController {

    private static final String X_REQUESTED_WITH = "X-Requested-With";
    private static final String XML_HTTP_REQUEST = "XMLHttpRequest";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final HealthEndpoint healthEndpoint;
    private final InfoEndpoint infoEndpoint;
    private final MeterRegistry meterRegistry;
    private final ApplicationEventPublisher publisher;

    @Value("${spring.application.version}")
    private String appVersion;

    @GetMapping("/dashboard")
    public String getDashboard(@AuthenticationPrincipal LoginDetails loginDetails,
                               ModelMap model,
                               HttpServletRequest request) {

        populateMetrics(model);

        publisher.publishEvent(ActivityEvent.builder()
                .loginEmail(loginDetails.getEmail())
                .eventType(ActivityType.ACTUATOR_DASHBOARD_VIEW)
                .metadata(Collections.singletonMap("actionBy", loginDetails.getEmail()))
                .internal(true)
                .build());

        if (XML_HTTP_REQUEST.equals(request.getHeader(X_REQUESTED_WITH))) {
            return "admin/actuator/actuatorDashboardContent :: dashboardContent";
        }

        return "admin/actuator/actuatorDashboard";
    }

    private void populateMetrics(ModelMap model) {
        HealthComponent health = healthEndpoint.health();
        model.put("healthStatus", health.getStatus().getCode());

        if (health instanceof CompositeHealth compositeHealth) {
            model.put("healthComponents", compositeHealth.getComponents());
        } else {
            model.put("healthComponents", Map.of());
        }

        double heapUsed = getGaugeValue("jvm.memory.used", "area", "heap");
        double heapMax = getGaugeValue("jvm.memory.max", "area", "heap");
        double cpuUsage = getGaugeValue("process.cpu.usage");
        double liveThreads = getGaugeValue("jvm.threads.live");
        double diskFree = getGaugeValue("disk.space.free");
        double diskTotal = getGaugeValue("disk.space.total");

        model.put("heapUsedMb", (long) (heapUsed / (1024 * 1024)));
        model.put("heapMaxMb", heapMax < 0 ? -1L : (long) (heapMax / (1024 * 1024)));
        model.put("heapUsedPercent", heapMax > 0 ? Math.round((heapUsed / heapMax) * 100) : 0L);
        model.put("cpuUsagePercent", Math.round(cpuUsage * 100));
        model.put("threadCount", (long) liveThreads);
        model.put("diskFreeMb", (long) (diskFree / (1024 * 1024)));
        model.put("diskTotalMb", (long) (diskTotal / (1024 * 1024)));
        model.put("diskUsedPercent", diskTotal > 0 ? Math.round(((diskTotal - diskFree) / diskTotal) * 100) : 0L);
        model.put("appInfo", infoEndpoint.info());
        model.put("lastRefreshed", LocalTime.now().format(TIME_FORMATTER));

        model.put("appVersion", appVersion);
    }

    private double getGaugeValue(String name, String... tags) {
        Search search = meterRegistry.find(name);

        for (int i = 0; i + 1 < tags.length; i += 2) {
            search = search.tag(tags[i], tags[i + 1]);
        }

        Gauge gauge = search.gauge();

        return gauge != null ? gauge.value() : 0.0;
    }
}

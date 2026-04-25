package dev.taskflow.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000L;
    private static final String AUTH_PREFIX = "/auth";

    private final Map<String, long[]> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getRequestURI().startsWith(AUTH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);

        long now = Instant.now().toEpochMilli();

        requestCounts.compute(clientIp, (ip, bucket) -> {
            if (bucket == null || now - bucket[1] > WINDOW_MS) {
                return new long[]{1, now};
            }
            bucket[0]++;
            return bucket;
        });

        long[] bucket = requestCounts.get(clientIp);
        if (bucket[0] > MAX_REQUESTS) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); //429
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"success": false, "message": "Too many requests. Please try again later."}
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request){
        String forwarded = request.getHeader("X-Forwarded-For"); //Set by load balancers and proxies
        if (forwarded != null && !forwarded.isBlank()){
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}

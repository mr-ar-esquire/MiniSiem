package com.siem.minisiem.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.InetAddress;

@Service
public class GeoIpService {

    private static final Logger logger = LoggerFactory.getLogger(GeoIpService.class);
    private DatabaseReader dbReader;

    public GeoIpService() {
        try {
            ClassPathResource resource = new ClassPathResource("GeoLite2-City.mmdb");

            if (resource.exists()) {
                InputStream inputStream = resource.getInputStream();
                dbReader = new DatabaseReader.Builder(inputStream).build();
                logger.info("GeoLite2 database loaded successfully!");
            } else {
                logger.warn("GeoLite2-City.mmdb not found in resources. Using fallback.");
            }

        } catch (Exception e) {
            logger.error("Error loading GeoIP database: {}", e.getMessage());
        }
    }

    public String getLocation(String ip) {

        // ❌ Invalid or empty
        if (ip == null || ip.trim().isEmpty() ||
                ip.equalsIgnoreCase("localhost") ||
                ip.equalsIgnoreCase("file-upload")) {
            return "Unknown";
        }

        // handle internal/private IP ranges separately
        if (ip.startsWith("192.168.") ||
                ip.startsWith("10.") ||
                ip.startsWith("172.") ||
                ip.equals("127.0.0.1") ||
                ip.equals("0:0:0:0:0:0:0:1")) {
            return "Internal Network";
        }

        // 🌍 External lookup
        if (dbReader != null) {
            try {
                InetAddress ipAddress = InetAddress.getByName(ip);
                CityResponse response = dbReader.city(ipAddress);

                String city = response.getCity().getName();
                String country = response.getCountry().getName();

                // Fallback handling
                if (city == null && country == null) {
                    return "Unknown";
                }

                if (city == null) {
                    return country;
                }

                if (country == null) {
                    return city;
                }

                return city + ", " + country;

            } catch (Exception e) {
                logger.warn("GeoIP lookup failed for {}: {}", ip, e.getMessage());
                return "Unknown";
            }
        }

        return "Unknown";
    }
}
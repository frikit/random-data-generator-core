/*
 * Copyright (c) 2026 random-data-generator contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.rdg.generator.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.rdg.generator.GeneratorConfig;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UrlGeneratorTest {

    @Test
    void testDefaultConstructor() {
        UrlGenerator generator = new UrlGenerator();
        assertNotNull(generator);
    }

    @Test
    void testLocaleConstructor() {
        UrlGenerator generator = new UrlGenerator(Locale.US);
        assertNotNull(generator);
    }

    @Test
    void testConfigConstructor() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        UrlGenerator generator = new UrlGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testNullConfigThrowsException() {
        assertThrows(NullPointerException.class, () -> new UrlGenerator((GeneratorConfig) null));
    }

    @Test
    void testGenerateNotNull() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generate();
        assertNotNull(url);
    }

    @Test
    void testGenerateHasProtocol() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generate();
        assertTrue(url.contains("://"), "URL should contain protocol separator");
    }

    @Test
    void testGenerateHasValidProtocol() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generate();
        assertTrue(
                url.startsWith("http://")
                        || url.startsWith("https://")
                        || url.startsWith("ftp://")
                        || url.startsWith("ws://")
                        || url.startsWith("wss://"),
                "URL should start with valid protocol");
    }

    @Test
    void testGenerateWithSpecificProtocol() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generate("https");
        assertTrue(url.startsWith("https://"));
    }

    @Test
    void testGenerateWithCustomProtocol() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generate("custom");
        assertTrue(url.startsWith("custom://"));
    }

    @Test
    void testGenerateWithNullProtocolThrowsException() {
        UrlGenerator generator = new UrlGenerator();
        assertThrows(NullPointerException.class, () -> generator.generate(null));
    }

    @Test
    void testGenerateWithPath() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generateWithPath();
        assertTrue(url.contains("/"), "URL with path should contain slash");
        int protocolEnd = url.indexOf("://") + 3;
        int firstSlash = url.indexOf("/", protocolEnd);
        assertTrue(firstSlash > protocolEnd, "URL should have path after domain");
    }

    @Test
    void testGenerateWithPathAndQuery() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generateWithPathAndQuery();
        assertTrue(url.contains("?"), "URL should contain query separator");
        assertTrue(url.contains("="), "URL should contain query parameter");
    }

    @Test
    void testBogusStyleUrlAliases() {
        UrlGenerator generator = new UrlGenerator(Locale.UK);
        assertTrue(generator.generateUrl().contains("://"));
        assertTrue(generator.generateUrlWithPath().contains("/"));
        assertTrue(Set.of("http", "https", "ftp", "ws", "wss").contains(generator.generateProtocol()));
    }

    @Test
    void testGetProtocol() {
        UrlGenerator generator = new UrlGenerator();
        String protocol = generator.getProtocol();
        assertNotNull(protocol);
        assertTrue(Set.of("http", "https", "ftp", "ws", "wss").contains(protocol));
    }

    @Test
    void testGetPath() {
        UrlGenerator generator = new UrlGenerator();
        String path = generator.getPath();
        assertNotNull(path);
        assertTrue(path.startsWith("/"), "Path should start with slash");
    }

    @Test
    void testGetQueryString() {
        UrlGenerator generator = new UrlGenerator();
        String query = generator.getQueryString();
        assertNotNull(query);
        assertFalse(query.startsWith("?"), "Query string should not start with ?");
        assertTrue(query.contains("="), "Query string should contain =");
    }

    @Test
    void testSeededGeneratorProducesSameResults() {
        UrlGenerator gen1 = new UrlGenerator(GeneratorConfig.builder().seed(42L).build());
        UrlGenerator gen2 = new UrlGenerator(GeneratorConfig.builder().seed(42L).build());

        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
    }

    @Test
    void testSeededGeneratorWithPathProducesSameResults() {
        UrlGenerator gen1 =
                new UrlGenerator(GeneratorConfig.builder().seed(999L).build());
        UrlGenerator gen2 =
                new UrlGenerator(GeneratorConfig.builder().seed(999L).build());

        assertEquals(gen1.generateWithPath(), gen2.generateWithPath());
        assertEquals(gen1.generateWithPath(), gen2.generateWithPath());
    }

    @Test
    void testSeededGeneratorWithPathAndQueryProducesSameResults() {
        UrlGenerator gen1 =
                new UrlGenerator(GeneratorConfig.builder().seed(777L).build());
        UrlGenerator gen2 =
                new UrlGenerator(GeneratorConfig.builder().seed(777L).build());

        assertEquals(gen1.generateWithPathAndQuery(), gen2.generateWithPathAndQuery());
    }

    @Test
    void testDifferentSeedsProduceDifferentResults() {
        UrlGenerator gen1 =
                new UrlGenerator(GeneratorConfig.builder().seed(100L).build());
        UrlGenerator gen2 =
                new UrlGenerator(GeneratorConfig.builder().seed(200L).build());

        assertNotEquals(gen1.generate(), gen2.generate());
    }

    @Test
    void testGenerateMultipleURLs() {
        UrlGenerator generator = new UrlGenerator();
        Set<String> urls = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            urls.add(generator.generate());
        }
        assertTrue(urls.size() > 10, "Should generate diverse URLs");
    }

    @Test
    void testGenerateMultipleProtocols() {
        UrlGenerator generator = new UrlGenerator();
        Set<String> protocols = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            protocols.add(generator.getProtocol());
        }
        assertTrue(protocols.size() >= 3, "Should generate multiple protocols");
    }

    @Test
    void testPathHasMultipleSegments() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(123L).build());
        Set<Integer> segmentCounts = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            String path = generator.getPath();
            int segments = path.split("/").length - 1; // -1 for leading slash
            segmentCounts.add(segments);
        }
        assertTrue(segmentCounts.size() > 1, "Should generate paths with varying segment counts");
    }

    @Test
    void testQueryStringHasMultipleParams() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(456L).build());
        Set<Integer> paramCounts = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            String query = generator.getQueryString();
            int params = query.split("&").length;
            paramCounts.add(params);
        }
        assertTrue(paramCounts.size() > 1, "Should generate queries with varying parameter counts");
    }

    @Test
    void testLocaleInfluencesDomain() {
        UrlGenerator deGenerator = new UrlGenerator(GeneratorConfig.builder()
                .locale(Locale.GERMANY)
                .networkSafetyPolicy(NetworkSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .build());
        Set<String> urls = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            urls.add(deGenerator.generate());
        }
        boolean hasDeUrl = urls.stream().anyMatch(u -> u.contains(".de"));
        assertTrue(hasDeUrl, "German locale should sometimes produce .de URLs");
    }

    @Test
    void testHTTPProtocol() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generate("http");
        assertTrue(url.startsWith("http://"));
        assertFalse(url.startsWith("https://"));
    }

    @Test
    void testHTTPSProtocol() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generate("https");
        assertTrue(url.startsWith("https://"));
    }

    @Test
    void testFTPProtocol() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generate("ftp");
        assertTrue(url.startsWith("ftp://"));
    }

    @Test
    void testWSProtocol() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generate("ws");
        assertTrue(url.startsWith("ws://"));
    }

    @Test
    void testWSSProtocol() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generate("wss");
        assertTrue(url.startsWith("wss://"));
    }

    @Test
    void testURLWithPathHasDomain() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generateWithPath();
        int protocolEnd = url.indexOf("://") + 3;
        int pathStart = url.indexOf("/", protocolEnd);
        String domain = url.substring(protocolEnd, pathStart);
        assertTrue(domain.contains("."), "Should have domain with TLD");
    }

    @Test
    void testURLWithQueryHasPath() {
        UrlGenerator generator = new UrlGenerator();
        String url = generator.generateWithPathAndQuery();
        int queryStart = url.indexOf("?");
        int protocolEnd = url.indexOf("://") + 3;
        String beforeQuery = url.substring(protocolEnd, queryStart);
        assertTrue(beforeQuery.contains("/"), "Should have path before query");
    }

    @Test
    void testQueryParametersHaveValues() {
        UrlGenerator generator = new UrlGenerator();
        String query = generator.getQueryString();
        String[] params = query.split("&");
        for (String param : params) {
            assertTrue(param.contains("="), "Each parameter should have a value");
            String[] parts = param.split("=");
            assertEquals(2, parts.length, "Parameter should have key and value");
        }
    }

    @Test
    void testPathSegmentsAreAlphabetic() {
        UrlGenerator generator = new UrlGenerator();
        for (int i = 0; i < 20; i++) {
            String path = generator.getPath();
            String[] segments = path.substring(1).split("/"); // Remove leading slash
            for (String segment : segments) {
                assertTrue(segment.matches("[a-z0-9]+"), "Path segment should be alphanumeric: " + segment);
            }
        }
    }

    @Test
    void testGenerateWithPathHasDifferentSegmentCounts() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(333L).build());
        Set<Integer> segmentCounts = new HashSet<>();

        for (int i = 0; i < 50; i++) {
            String path = generator.getPath();
            int segments = path.split("/").length - 1; // -1 for leading slash
            segmentCounts.add(segments);
        }

        assertTrue(segmentCounts.size() > 1, "Should generate paths with varying segment counts");
    }

    @Test
    void testGenerateWithQueryHasDifferentParamCounts() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(444L).build());
        Set<Integer> paramCounts = new HashSet<>();

        for (int i = 0; i < 50; i++) {
            String query = generator.getQueryString();
            int params = query.split("&").length;
            paramCounts.add(params);
        }

        assertTrue(paramCounts.size() > 1, "Should generate queries with varying parameter counts");
    }

    @Test
    void testSeededGeneratorGetProtocolConsistency() {
        UrlGenerator gen1 =
                new UrlGenerator(GeneratorConfig.builder().seed(666L).build());
        UrlGenerator gen2 =
                new UrlGenerator(GeneratorConfig.builder().seed(666L).build());

        for (int i = 0; i < 10; i++) {
            assertEquals(gen1.getProtocol(), gen2.getProtocol());
        }
    }

    @Test
    void testSeededGeneratorGetPathConsistency() {
        UrlGenerator gen1 =
                new UrlGenerator(GeneratorConfig.builder().seed(777L).build());
        UrlGenerator gen2 =
                new UrlGenerator(GeneratorConfig.builder().seed(777L).build());

        for (int i = 0; i < 10; i++) {
            assertEquals(gen1.getPath(), gen2.getPath());
        }
    }

    @Test
    void testSeededGeneratorGetQueryStringConsistency() {
        UrlGenerator gen1 =
                new UrlGenerator(GeneratorConfig.builder().seed(888L).build());
        UrlGenerator gen2 =
                new UrlGenerator(GeneratorConfig.builder().seed(888L).build());

        for (int i = 0; i < 10; i++) {
            assertEquals(gen1.getQueryString(), gen2.getQueryString());
        }
    }

    @Test
    void testGenerateWithDomain() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(1L).build());
        String url = generator.generateWithDomain("example.com");
        assertTrue(url.contains("://example.com"));
    }

    @Test
    void testGenerateWithDomainNullThrows() {
        UrlGenerator generator = new UrlGenerator();
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithDomain(null));
    }

    @Test
    void testGenerateWithDomainPrefix() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(2L).build());
        String url = generator.generateWithDomainPrefix("api");
        assertTrue(url.contains("://api."));
    }

    @Test
    void testGenerateWithTrailingSlashPathAndExtension() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(3L).build());
        UrlGenerator.UrlOptions options =
                new UrlGenerator.UrlOptions("https", "example.com", null, "/", new String[] {"txt"}, false);
        String url = generator.generateWithOptions(options);
        assertTrue(url.startsWith("https://example.com/"));
        assertTrue(url.endsWith(".txt"));
    }

    @Test
    void testGenerateWithDomainPrefixNullThrows() {
        UrlGenerator generator = new UrlGenerator();
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithDomainPrefix(null));
    }

    @Test
    void testGenerateWithFixedPath() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(3L).build());
        String url = generator.generateWithFixedPath("/api/v1");
        assertTrue(url.contains("/api/v1"));
    }

    @Test
    void testGenerateWithFixedPathNullThrows() {
        UrlGenerator generator = new UrlGenerator();
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithFixedPath(null));
    }

    @Test
    void testGenerateWithFixedPathWithoutLeadingSlash() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(4L).build());
        String url = generator.generateWithFixedPath("api/v1");
        assertTrue(url.contains("/api/v1"));
    }

    @Test
    void testGenerateWithExtensions() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(5L).build());
        String url = generator.generateWithExtensions("gif", "jpg");
        assertTrue(url.matches(".*\\.(gif|jpg)$"));
        assertTrue(url.contains("/"));
    }

    @Test
    void testGenerateWithOptionsComposite() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(6L).build());
        UrlGenerator.UrlOptions options =
                new UrlGenerator.UrlOptions("https", "example.com", "cdn", "/assets/logo", new String[] {"png"}, true);
        String url = generator.generateWithOptions(options);
        assertTrue(url.startsWith("https://cdn.example.com/assets/logo/"));
        assertTrue(url.contains(".png?"));
    }

    @Test
    void testGenerateWithOptionsNullThrows() {
        UrlGenerator generator = new UrlGenerator();
        assertThrows(NullPointerException.class, () -> generator.generateWithOptions(null));
    }

    @Test
    void testGenerateWithOptionsBlankDomainThrows() {
        UrlGenerator generator = new UrlGenerator();
        UrlGenerator.UrlOptions options = new UrlGenerator.UrlOptions("https", " ", null, null, null, false);
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithOptions(options));
    }

    @Test
    void testGenerateWithOptionsBlankPathThrows() {
        UrlGenerator generator = new UrlGenerator();
        UrlGenerator.UrlOptions options = new UrlGenerator.UrlOptions(null, null, null, " ", null, false);
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithOptions(options));
    }

    @Test
    void testGenerateWithOptionsEmptyExtensionsThrows() {
        UrlGenerator generator = new UrlGenerator();
        UrlGenerator.UrlOptions options = new UrlGenerator.UrlOptions(null, null, null, null, new String[0], false);
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithOptions(options));
    }

    @Test
    void testGenerateWithOptionsBlankExtensionThrows() {
        UrlGenerator generator = new UrlGenerator();
        UrlGenerator.UrlOptions options =
                new UrlGenerator.UrlOptions(null, null, null, null, new String[] {" "}, false);
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithOptions(options));
    }

    @Test
    void testGenerateWithOptionsNormalizesDotExtensions() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(7L).build());
        UrlGenerator.UrlOptions options = new UrlGenerator.UrlOptions(
                "https", "example.com", null, "/download/file", new String[] {".zip"}, false);
        String url = generator.generateWithOptions(options);
        assertTrue(url.contains(".zip"));
        assertTrue(url.contains("%20") || url.contains("-"));
    }

    @Test
    void testGenerateWithOptionsQueryWithoutPathCreatesPath() {
        UrlGenerator generator =
                new UrlGenerator(GeneratorConfig.builder().seed(8L).build());
        UrlGenerator.UrlOptions options = new UrlGenerator.UrlOptions("https", "example.com", null, null, null, true);
        String url = generator.generateWithOptions(options);
        assertTrue(url.contains("/"));
        assertTrue(url.contains("?"));
    }

    @Test
    void testDefaultExtensionsExposed() {
        String[] extensions = UrlGenerator.UrlOptions.defaultExtensions();
        assertNotNull(extensions);
        assertTrue(extensions.length > 0);
    }

    @Test
    void testUrlOptionsDefaultConstructor() {
        UrlGenerator.UrlOptions options = new UrlGenerator.UrlOptions();
        assertNull(options.protocol());
        assertNull(options.domain());
        assertNull(options.domainPrefix());
        assertNull(options.path());
        assertNull(options.extensions());
        assertFalse(options.withQuery());
    }

    @Test
    void testGenerateWithSpecificProtocolConsistentWithSeed() {
        UrlGenerator gen1 =
                new UrlGenerator(GeneratorConfig.builder().seed(999L).build());
        UrlGenerator gen2 =
                new UrlGenerator(GeneratorConfig.builder().seed(999L).build());

        assertEquals(gen1.generate("ftp"), gen2.generate("ftp"));
        assertEquals(gen1.generate("ws"), gen2.generate("ws"));
    }

    @Test
    void testAllProtocolsGenerated() {
        UrlGenerator generator = new UrlGenerator();
        Set<String> protocols = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            String protocol = generator.getProtocol();
            protocols.add(protocol);
        }

        assertTrue(protocols.size() >= 3, "Should generate at least 3 different protocols");
    }

    @Test
    void testQueryValuesAreNumeric() {
        UrlGenerator generator = new UrlGenerator();
        String query = generator.getQueryString();
        String[] params = query.split("&");

        for (String param : params) {
            String[] parts = param.split("=");
            String value = parts[1];
            assertTrue(value.matches("\\d+"), "Query value should be numeric: " + value);
        }
    }

    @Test
    void testGeneratePathWithSingleSegment() {
        UrlGenerator gen = new UrlGenerator(GeneratorConfig.builder().seed(1L).build());

        boolean foundSingleSegment = false;
        for (int i = 0; i < 100; i++) {
            String path = gen.getPath();
            int segments = path.split("/").length - 1;
            if (segments == 1) {
                foundSingleSegment = true;
                break;
            }
        }

        assertTrue(foundSingleSegment, "Should generate paths with single segment");
    }

    @Test
    void testGeneratePathWithMultipleSegments() {
        UrlGenerator gen = new UrlGenerator(GeneratorConfig.builder().seed(2L).build());

        boolean foundMultipleSegments = false;
        for (int i = 0; i < 100; i++) {
            String path = gen.getPath();
            int segments = path.split("/").length - 1;
            if (segments > 1) {
                foundMultipleSegments = true;
                break;
            }
        }

        assertTrue(foundMultipleSegments, "Should generate paths with multiple segments");
    }

    @Test
    void testGenerateQueryWithSingleParam() {
        UrlGenerator gen = new UrlGenerator(GeneratorConfig.builder().seed(3L).build());

        boolean foundSingleParam = false;
        for (int i = 0; i < 100; i++) {
            String query = gen.getQueryString();
            int params = query.split("&").length;
            if (params == 1) {
                foundSingleParam = true;
                break;
            }
        }

        assertTrue(foundSingleParam, "Should generate queries with single parameter");
    }

    @Test
    void testGenerateQueryWithMultipleParams() {
        UrlGenerator gen = new UrlGenerator(GeneratorConfig.builder().seed(4L).build());

        boolean foundMultipleParams = false;
        for (int i = 0; i < 100; i++) {
            String query = gen.getQueryString();
            int params = query.split("&").length;
            if (params > 1) {
                foundMultipleParams = true;
                break;
            }
        }

        assertTrue(foundMultipleParams, "Should generate queries with multiple parameters");
    }

    @Test
    void testQueryStringLoopBranchCoverage() {
        UrlGenerator gen =
                new UrlGenerator(GeneratorConfig.builder().seed(5555L).build());

        // Generate many queries to ensure loop iterations cover all branches
        for (int i = 0; i < 50; i++) {
            String query = gen.getQueryString();
            assertFalse(query.isEmpty());

            String[] parts = query.split("&");
            assertTrue(parts.length >= 1 && parts.length <= 3, "Query should have 1-3 parameters");
        }
    }

    @Test
    void testPathLoopBranchCoverage() {
        UrlGenerator gen =
                new UrlGenerator(GeneratorConfig.builder().seed(6666L).build());

        // Generate many paths to ensure loop iterations cover all branches
        for (int i = 0; i < 50; i++) {
            String path = gen.getPath();
            assertTrue(path.startsWith("/"));

            int segments = path.split("/").length - 1;
            assertTrue(segments >= 1 && segments <= 3, "Path should have 1-3 segments");
        }
    }

    @Test
    void testQueryAndTldAliases() {
        UrlGenerator generator = new UrlGenerator(Locale.US);
        String query = generator.generateQuery();
        String tld = generator.generateTld();
        assertFalse(query.startsWith("?"));
        assertFalse(query.isBlank());
        assertFalse(tld.isBlank());
    }
}

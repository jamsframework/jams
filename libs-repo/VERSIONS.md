# libs-repo: Origins and Actual Versions

All artifacts in this repository use the coordinates `jams.ext:<artifactId>:local`.
This file documents what the jars actually contain (version according to
manifest or file name) and where they would be publicly available.

As of: 2026-07-15. Coordinates marked with + were verified via HTTP request
against the respective repository (exact version available); entries without
a + are plausible but unverified.

## 1. Available on Maven Central (exact version)

| Artifact (jams.ext) | Version | Maven coordinates |
|---|---|---|
| Jama-1.0.2 | 1.0.2 | + `gov.nist.math:jama:1.0.2` |
| aopalliance-repackaged-2.3.0-b05 | 2.3.0-b05 | + `org.glassfish.hk2.external:aopalliance-repackaged:2.3.0-b05` |
| apache-mime4j-0.6 | 0.6 | + `org.apache.james:apache-mime4j:0.6` |
| asm-all-repackaged-2.2.0 | 2.2.0 (ASM 5.0_BETA) | + `org.glassfish.hk2.external:asm-all-repackaged:2.2.0-b21` (b21 variant) |
| asm-debug-all-5.0.2 | 5.0.2 | + `org.ow2.asm:asm-debug-all:5.0.2` |
| avalon-framework-4.2.0 | **4.1.5** (manifest!) | + `avalon-framework:avalon-framework:4.1.5` |
| colt | presumably 1.2.0 (no version in manifest) | + `colt:colt:1.2.0` |
| commons-beanutils-1.7.0 | 1.7.0 | + `commons-beanutils:commons-beanutils:1.7.0` |
| commons-codec-1.9 | 1.9 | + `commons-codec:commons-codec:1.9` |
| commons-io-1.3.1 | 1.3.1 | + `commons-io:commons-io:1.3.1` |
| commons-lang3-3.1 | 3.1 | + `org.apache.commons:commons-lang3:3.1` |
| commons-logging-1.1.3 | 1.1.3 | + `commons-logging:commons-logging:1.1.3` |
| commons-math-1.2 | 1.2 | + `commons-math:commons-math:1.2` |
| commons-math3-3.2 | 3.2 | + `org.apache.commons:commons-math3:3.2` |
| encog-core-2.5.3 | 2.5.3 | + `org.encog:encog-core:2.5.3` |
| encog-engine-2.5.3 | 2.5.3 | `org.encog:encog-engine:2.5.3` |
| exp4j-0.4.8 | 0.4.8 | + `net.objecthunter:exp4j:0.4.8` |
| fastutil-6.5.9 | 6.5.9 | + `it.unimi.dsi:fastutil:6.5.9` |
| fluent-hc-4.3.3 | 4.3.3 | + `org.apache.httpcomponents:fluent-hc:4.3.3` |
| flyway-core-3.2.1 | 3.2.1 | + `org.flywaydb:flyway-core:3.2.1` |
| fop | **0.95** (not 1.0) | + `org.apache.xmlgraphics:fop:0.95` |
| groovy-all | 1.6.4 | + `org.codehaus.groovy:groovy-all:1.6.4` |
| h2-1.3.169 | 1.3.169 | + `com.h2database:h2:1.3.169` |
| hibernate-jpa-2.0-api-1.0.1.Final | 1.0.1.Final | + `org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.1.Final` |
| hk2-api / hk2-locator / hk2-utils 2.3.0-b05 | 2.3.0-b05 | + `org.glassfish.hk2:hk2-api|hk2-locator|hk2-utils:2.3.0-b05` |
| httpclient-4.3.3 / httpclient-cache-4.3.3 / httpmime-4.3.3 | 4.3.3 | + `org.apache.httpcomponents:*:4.3.3` |
| httpcore-4.3.2 | 4.3.2 | + `org.apache.httpcomponents:httpcore:4.3.2` |
| javassist-3.18.1-GA | 3.18.1-GA | + `org.javassist:javassist:3.18.1-GA` |
| javax.activation-1.2.0 | 1.2.0 | + `com.sun.activation:javax.activation:1.2.0` |
| javax.annotation-api | 1.2 | + `javax.annotation:javax.annotation-api:1.2` |
| javax.el | 3.0.0 | + `org.glassfish:javax.el:3.0.0` |
| javax.inject | 1 | + `javax.inject:javax.inject:1` |
| javax.json | 1.0 | + `org.glassfish:javax.json:1.0` |
| javax.mail | 1.5.0 | + `com.sun.mail:javax.mail:1.5.0` |
| javax.servlet-api | 3.1.0 | + `javax.servlet:javax.servlet-api:3.1.0` |
| javax.ws.rs-api | 2.0 | + `javax.ws.rs:javax.ws.rs-api:2.0` |
| jaxb-api-2.2.7 | 2.2.7 | + `javax.xml.bind:jaxb-api:2.2.7` |
| jcommon-1.0.21 | 1.0.21 | + `org.jfree:jcommon:1.0.21` |
| jersey-client / -common / -server | 2.9 | + `org.glassfish.jersey.core:jersey-client|jersey-common|jersey-server:2.9` |
| jersey-container-servlet(-core) | 2.9 | + `org.glassfish.jersey.containers:*:2.9` |
| jersey-guava-2.9 | 2.9 | + `org.glassfish.jersey.bundles.repackaged:jersey-guava:2.9` |
| jersey-media-multipart-2.9 | 2.9 | + `org.glassfish.jersey.media:jersey-media-multipart:2.9` |
| jeuclid-core-3.1.9 | 3.1.9 | + `net.sourceforge.jeuclid:jeuclid-core:3.1.9` |
| jeuclid-fop-3.1.9 | 3.1.9 | `net.sourceforge.jeuclid:jeuclid-fop:3.1.9` |
| jfreechart-1.0.17 | 1.0.17 | + `org.jfree:jfreechart:1.0.17` |
| jna | 3.3.0 | + `net.java.dev.jna:jna:3.3.0` |
| json-20180813 | 20180813 | + `org.json:json:20180813` |
| jsr-275-1.0-beta-2 | 1.0-beta-2 | + `net.java.dev.jsr-275:jsr-275:1.0-beta-2` |
| juel-api / -impl / -spi 2.2.6 | 2.2.6 | + `de.odysseus.juel:juel-api|juel-impl|juel-spi:2.2.6` |
| junit-4.5 | 4.5 | + `junit:junit:4.5` |
| logback-classic / -core 1.0.13 | 1.0.13 | + `ch.qos.logback:logback-classic|logback-core:1.0.13` |
| looks-2.1.4 | 2.1.4 | + `com.jgoodies:looks:2.1.4` |
| mimepull-1.9.4 | 1.9.4 | + `org.jvnet.mimepull:mimepull:1.9.4` |
| mysql-connector-java-5.1.13-bin | 5.1.13 | + `mysql:mysql-connector-java:5.1.13` |
| org.osgi.core-4.2.0 | 4.2.0 | + `org.osgi:org.osgi.core:4.2.0` |
| osgi-resource-locator-1.0.1 | 1.0.1 | + `org.glassfish.hk2:osgi-resource-locator:1.0.1` |
| persistence-api-1.0 | 1.0 | + `javax.persistence:persistence-api:1.0` |
| platform | 3.3.0 (JNA platform) | `net.java.dev.jna:platform:3.3.0` |
| slf4j-api-1.6.1 / -1.7.5 | 1.6.1 / 1.7.5 | + `org.slf4j:slf4j-api:1.6.1|1.7.5` |
| slf4j-jdk14-1.6.1 | 1.6.1 | + `org.slf4j:slf4j-jdk14:1.6.1` |
| trove-3.0.3 | 3.0.3 | + `net.sf.trove4j:trove4j:3.0.3` |
| validation-api-1.1.0.Final | 1.1.0.Final | + `javax.validation:validation-api:1.1.0.Final` |
| vecmath | 1.5.2 | + `javax.vecmath:vecmath:1.5.2` |
| xml-apis-1.3.04 / xml-apis-ext-1.3.04 | 1.3.04 | + `xml-apis:xml-apis|xml-apis-ext:1.3.04` |
| xmlgraphics-commons-1.3.1 | 1.3.1 | + `org.apache.xmlgraphics:xmlgraphics-commons:1.3.1` |

## 2. In the OSGeo repository (https://repo.osgeo.org/repository/release)

| Artifact | Version | Coordinates |
|---|---|---|
| gt-api, gt-coverage, gt-cql, gt-data, gt-epsg-extension, gt-epsg-wkt, gt-legacy, gt-main, gt-metadata, gt-referencing, gt-referencing3D, gt-render, gt-shapefile, gt-shapefile-renderer | 2.5.2 | + `org.geotools:gt-*:2.5.2` (gt-main, gt-shapefile verified) |
| gt-mappane | 2.5.5 | + `org.geotools:gt-mappane:2.5.5` |
| geoapi-2.2-M1 | 2.2-M1 | + `org.opengis:geoapi:2.2-M1` |
| jai_core | 1.1.3 | + `javax.media:jai_core:1.1.3` (jar missing on Central, POM only) |
| jai_codec | 1.1.3 | + `javax.media:jai_codec:1.1.3` |
| jts-1.9 | 1.9 | `com.vividsolutions:jts:1.9` (not on Central — only 1.8 and 1.11+ there) |

## 3. Other public sources

| Artifact | Version | Source |
|---|---|---|
| gluegen-rt, jogl-all (+ natives linux-amd64, macosx-universal, windows-amd64) | **2.6.0** | + https://jogamp.org/deployment/archive/rc/v2.6.0/jar (not on Central, which only has ≤ 2.3.2) |
| gluegen-rt-/jogl-all-natives-\*-i586 | **2.1.5** (old!) | 32-bit natives no longer exist as of JogAmp 2.5; legacy leftovers |
| worldwind, worldwindx, vpf-symbols | 2.2.1 | + GitHub release `NASAWorldWind/WorldWindJava` v2.2.1 (worldwind-v2.2.1.zip) |
| gdal | unknown | GDAL Java bindings from the WorldWind distribution |
| netcdfAll-5.5.3 | 5.5.3 | Unidata (`edu.ucar:netcdfAll`); currently not found in the Unidata Maven repo, obtain via Unidata downloads |
| batik-all-1.7 | 1.7+r608262 | Central only has individual Batik modules at 1.7, `batik-all` starting at 1.10 |
| jcommon-1.0.18 | 1.0.18 | not on Central (1.0.21 is, and is also present here) |
| postgresql-8.2-506.jdbc4 | 8.2-506.jdbc4 | build 506 not on Central (504/507 exist); modern replacement: `org.postgresql:postgresql` |
| javax.faces | 2.2.0 (Mojarra) | GlassFish 4 distribution; Central: `org.glassfish:javax.faces` |
| javax.persistence | 2.1.0 (EclipseLink) | GlassFish/EclipseLink; Central: `org.eclipse.persistence:javax.persistence` |
| jaxb-osgi / jaxb-api-osgi | 2.2.7 | GlassFish 4 distribution (JAXB RI OSGi bundles) |
| webservices-osgi / webservices-api-osgi | 2.3 (Metro) | GlassFish 4 distribution |
| weld-osgi-bundle | 2.x (build 20130513) | GlassFish 4 distribution |
| bean-validator | Hibernate Validator 5.0.0.Final (repackaged) | GlassFish 4 distribution |
| javax.batch-api (1.0), javax.ejb-api (3.2), javax.jms-api (2.0), javax.interceptor-api (1.2), javax.transaction-api (1.2), javax.websocket-api (1.0), javax.resource-api (1.7), javax.security.auth.message-api (1.1), javax.security.jacc-api (1.5), javax.management.j2ee-api (1.1.1), javax.enterprise.concurrent(-api) (1.0), javax.enterprise.deploy-api (1.6), javax.servlet.jsp(-api) (2.3.x), javax.servlet.jsp.jstl(-api) (1.2.x), javax.xml.registry-api (1.0.5), javax.xml.rpc-api (1.1.1), jaxm-api | see parentheses | GlassFish 4 distribution; most of these likely exist on Central (coordinates to be verified per artifact) |
| j3dcore, j3dutils | 1.5.2 | Java3D 1.5.2; publicly only 1.3.1 (`java3d`) or 1.6.0 (`org.scijava`) — 1.5.2 is not in any Maven repo |
| swingx | unknown | SwingLabs SwingX; Central: `org.swinglabs:swingx` (version would need to be determined by class comparison) |
| junit (no version) | unknown (empty manifest, presumably 3.8.x) | Central: `junit:junit` |
| ssj | 2.5 (2012) | Université de Montréal; Central only has newer 3.x (`ca.umontreal.iro.simul:ssj`) |

## 4. No public Maven source known (must remain local)

| Artifact | Version / origin |
|---|---|
| Blas | Fortran BLAS port, origin unknown |
| JRI | rJava/JRI (R integration); normally installed with the R package rJava |
| JOCL-0.1.3a-beta | jocl.org; Central only has newer `org.jocl:jocl` |
| fits, fitsobj | FITS format wrappers (HDFView ecosystem) |
| jhdf, jhdf4obj, jhdf5, jhdf5obj, jhdfobj, jhdfview, nc2obj | HDF Java wrappers from the HDFView 2.x distribution (HDF Group) |
| ngmf, ngmf.ext | OMS3/NGMF framework (USDA/CSU) |
| jgt-jgrassgears-0.7.9-SNAPSHOT | JGrasstools, SNAPSHOT build |
| event-1.6.5, interpreter-1.6.8, language-1.6.7, logger-1.6.4 | DSOL simulation 1.6.x (TU Delft); 1.6.x not in public repos (DSOL 3.x would be on Central) |
| tcode | 1.0 Beta (2003), Université de Montréal (SSJ ecosystem) |
| units-0.01 | JSR-108 reference implementation (SourceForge, project dead) |
| optimization | origin unknown |
| plugin, wizard | origin unknown (presumably NetBeans ecosystem) |

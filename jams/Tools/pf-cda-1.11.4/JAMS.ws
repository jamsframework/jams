<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE Workset SYSTEM "http://dependency-analyzer.org/schema/dtd/workset-1.6.dtd">
<Workset version="6">
  <WorksetName>JAMS</WorksetName>
  <Options auto-reload="no" />
  <Classpath shortContainerNames="yes">
    <ClasspathPart type="bin-class">../../lib/**/*.jar</ClasspathPart>
    <ClasspathPart type="bin-class">D:/jamsdeploy/lib/JAMS.jar</ClasspathPart>
    <ClasspathPart type="bin-class">D:/jamsdeploy/lib/JAMSComponents.jar</ClasspathPart>
  </Classpath>
  <ViewFilters>
    <PatternFilter active="yes">java.*</PatternFilter>
    <PatternFilter active="yes">javax.*</PatternFilter>
    <PatternFilter active="yes">com.sun.*</PatternFilter>
  </ViewFilters>
  <IgnoreFilters>
    <PatternFilter active="no">java.*</PatternFilter>
    <PatternFilter active="no">javax.*</PatternFilter>
    <PatternFilter active="no">com.sun.*</PatternFilter>
  </IgnoreFilters>
  <Architecture>
    <ComponentModel name="Default" />
  </Architecture>
</Workset>
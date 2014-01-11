<StyledLayerDescriptor version="0.7.2" xmlns:ogc="http://www.opengis.net/ogc">
<NamedLayer>
<Name>Layer Description File for HRU Shape</Name>
<UserStyle>
 <Name>HRUs, standard</Name>
 <FeatureTypeStyle>
  <rule>
   <PolygonSymbolizer>
    <Fill>
     <CssParameter name="fill">#FFF053</CssParameter>
    </Fill>
    <Stroke>
     <CssParameter name="stroke">#000000</CssParameter>
     <CssParameter name="stroke-width">1</CssParameter>
    </Stroke>
   </PolygonSymbolizer>
   <!--<TextSymbolizer>
     <Label>
      <ogc:PropertyName>newAt</ogc:PropertyName>
     </Label>
     <Font>
      <CssParameter name="font-family">Lucida Sans</CssParameter>
      <CssParameter name="fontstyle"> italic</CssParameter>
      <CssParameter name="font-size">12</CssParameter>
     </Font>
     <Fill>
      <CssParameter name="fill">#000000</CssParameter>
     </Fill>
     <Halo>
      <CssParameter name="fill">#FFFFFF</CssParameter>
      <Radius>2</Radius>
     </Halo>
    </TextSymbolizer>-->
  </rule>
 </FeatureTypeStyle>
</UserStyle>
<UserStyle>
 <Name>HRUs, classified</Name>
 <FeatureTypeStyle>
  <rule>
   <ogc:Filter>
    <ogc:PropertyIsBetween>
     <ogc:PropertyName>newAt</ogc:PropertyName>
     <ogc:LowerBoundary><ogc:Literal>3.0</ogc:Literal></ogc:LowerBoundary>
     <ogc:UpperBoundary><ogc:Literal>3.2</ogc:Literal></ogc:UpperBoundary>
    </ogc:PropertyIsBetween>
   </ogc:Filter>
   <PolygonSymbolizer>
    <Fill>
     <CssParameter name="fill">#800080</CssParameter>
    </Fill>
    <Stroke>
     <CssParameter name="stroke">#000000</CssParameter>
     <CssParameter name="stroke-width">1</CssParameter>
    </Stroke>
   </PolygonSymbolizer>
  </rule>
  <rule>
   <ogc:Filter>
    <ogc:PropertyIsBetween>
     <ogc:PropertyName>newAt</ogc:PropertyName>
     <ogc:LowerBoundary><ogc:Literal>3.2</ogc:Literal></ogc:LowerBoundary>
     <ogc:UpperBoundary><ogc:Literal>3.4</ogc:Literal></ogc:UpperBoundary>
    </ogc:PropertyIsBetween>
   </ogc:Filter>
   <PolygonSymbolizer>
    <Fill>
     <CssParameter name="fill">#00FF00</CssParameter>
    </Fill>
    <Stroke>
     <CssParameter name="stroke">#000000</CssParameter>
     <CssParameter name="stroke-width">1</CssParameter>
    </Stroke>
   </PolygonSymbolizer>
  </rule>
  <rule>
   <ogc:Filter>
    <ogc:PropertyIsBetween>
     <ogc:PropertyName>newAt</ogc:PropertyName>
     <ogc:LowerBoundary><ogc:Literal>3.4</ogc:Literal></ogc:LowerBoundary>
     <ogc:UpperBoundary><ogc:Literal>3.6</ogc:Literal></ogc:UpperBoundary>
    </ogc:PropertyIsBetween>
   </ogc:Filter>
   <PolygonSymbolizer>
    <Fill>
     <CssParameter name="fill">#00FFFF</CssParameter>
    </Fill>
    <Stroke>
     <CssParameter name="stroke">#000000</CssParameter>
     <CssParameter name="stroke-width">1</CssParameter>
    </Stroke>
   </PolygonSymbolizer>
  </rule>
<rule>
   <ogc:Filter>
    <ogc:PropertyIsBetween>
     <ogc:PropertyName>newAt</ogc:PropertyName>
     <ogc:LowerBoundary><ogc:Literal>3.6</ogc:Literal></ogc:LowerBoundary>
     <ogc:UpperBoundary><ogc:Literal>3.8</ogc:Literal></ogc:UpperBoundary>
    </ogc:PropertyIsBetween>
   </ogc:Filter>
   <PolygonSymbolizer>
    <Fill>
     <CssParameter name="fill">#FFFFFF</CssParameter>
    </Fill>
    <Stroke>
     <CssParameter name="stroke">#000000</CssParameter>
     <CssParameter name="stroke-width">1</CssParameter>
    </Stroke>
   </PolygonSymbolizer>
  </rule>
 </FeatureTypeStyle>
</UserStyle>
<UserStyle>
 <Name>HRUs, selected</Name>
  <FeatureTypeStyle>
   <rule>
    <PolygonSymbolizer>
     <Fill>
      <CssParameter name="fill">#FFFF00</CssParameter>
     </Fill>
    </PolygonSymbolizer>
    <TextSymbolizer>
     <Label>
      <ogc:PropertyName>newAt</ogc:PropertyName>
     </Label>
     <Font>
      <CssParameter name="font-family">Lucida Sans</CssParameter>
      <CssParameter name="fontstyle"> italic</CssParameter>
      <CssParameter name="font-size">12</CssParameter>
     </Font>
     <Fill>
      <CssParameter name="fill">#000000</CssParameter>
     </Fill>
     <Halo>
      <CssParameter name="fill">#FFFFFF</CssParameter>
      <Radius>2</Radius>
     </Halo>
    </TextSymbolizer>
   </rule>
  </FeatureTypeStyle>
 </UserStyle>
</NamedLayer>
</StyledLayerDescriptor>
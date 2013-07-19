<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
<xsl:output method="html" indent="yes"/>

<xsl:template match="/">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
<xsl:call-template name="css_and_js" />
<xsl:apply-templates select="*" mode="hack" />
<xsl:if test="0">
<xsl:if test="not(DASDNA) and not(DASSEQUENCE)">
  <xsl:call-template name="table_sort_js" />
</xsl:if>
</xsl:if>
  <title><xsl:apply-templates select="*" mode="title" /></title>
  </head>
  <body>

<div id="masthead">
  <h1><a href="/"><img src="/img/e-bang.gif" style="width: 46px; height: 40px; vertical-align:bottom; border:0px; padding-bottom:2px" alt="" title="Home" /></a><a href="http://www.derkholm.net/thomas/dazzle/" class="home serif">Dazzle server</a>
  <xsl:apply-templates select="*" mode="masthead" />
  </h1>
</div>

<xsl:call-template name="release_and_links" />
<div id="page">
<div class="sptop">&#160;</div>
<xsl:apply-templates select="*" />
<xsl:call-template name="disclaimer"        />
</div>
  </body>
</html>
</xsl:template>

<xsl:template name="hack_js">
  <script type="text/javascript">//<![CDATA[
  function fix_species( ) {
    var URL      = document.location.href;
    var testRe   = new RegExp('^(https?://[^/]+)(/?.*/das/)([^/]*)/([^?]+)');
    var T        = URL.match(testRe);
    var DAS_BIT  = '';
    var DAS_URL  = '';
    var base_URL = '';
    var DSN      = '';
    var action   = '';
    var species  = 'DAS';
    var species_name;
    var asm      = '';
    var type     = '';
    if( T ) {
      base_URL = T[1];
      DAS_bit  = T[2];
      DAS_URL  = T[1]+T[2];    
      DSN      = T[3];
      action   = T[4];
      T = DSN.match(/^([^.]+)\.(.*)\.([^.]+)$/);
      species  = T[1];
      asm      = T[2];
      type     = T[3];
    }
    var species_name = species.replace(/_/,' ');
    var Els          = document.getElementsByTagName('i');
    for(i=0;i<Els.length;i++) {
      var T = Els[i];
      T.innerHTML = species_name;
    }
    var T = document.getElementsByTagName('title');
    var Q = T[0].innerHTML;
    Q = Q.replace(/DAS/,species_name);
    T[0].innerHTML = Q;
    var T = document.getElementById('masthead_species');
    T.setAttribute('href',base_URL+'/'+species+'/');
  }
  addLoadEvent( fix_species );
//]]>
  </script>
</xsl:template>

<xsl:template name="table_sort_js">
  <script type="text/javascript" src="/js/tablesorter.js"></script>
  <script type="text/javascript">//<![CDATA[
  function sort_table() {
    var TableSorter1 = new TSorter;
    TableSorter1.init('das');
  }
  addLoadEvent( sort_table );
//]]>
  </script>
</xsl:template>
<xsl:template name="css_and_js">
  <script type="text/javascript">//<![CDATA[
function addLoadEvent(func) {
  var oldonload = window.onload;
  if( typeof window.onload != 'function' ) { window.onload = func; } else { window.onload = function() { oldonload(); func(); } }
}
//]]>
  </script>
  <style type="text/css" media="all">
    @import url(css/dazzle.css);
    @import url(css/content.css);
div.das { text-align: center }
table.das { border-collapse: collapse; border: 1px solid #ccc; width: 98%; margin: 0px auto; text-align: left }
table.das tr    { background:#fff; vertical-align: top }
table.das tr th { border:1px solid #ccc; background: #ccc; text-align: center }
table.das tr td { border:1px solid #ccc; }
table.das tr td.e { background-color: #fdd }
table.das tr td.c { text-align: center }
table.das tr td.l { text-align: left  }
table.das tr td.r { text-align: right }
table.das tr td ul     { margin: 0px; padding: 0px }
#page div.das table.das tr td ul li  { margin: 0px; list-style-type: none; list-style-image: none; }
#page div.das table.das tr dt ul li span.nb, #page table.das tr td ul li a { white-space: nowrap; font-weight: bold; text-decoration: none }
table.das tr.alt_bg td { background-color:#eee }
table.das tr.ref_bg td { background-color:#fed }
  </style>
  <style type="text/css" media="print">
    @import url(css/printer-styles.css);
  </style>
  <style type="text/css" media="screen">
    @import url(css/screen-styles.css);
  </style>
  <style type="text/css" media="all">
body { background-image: none }
  </style>
</xsl:template>

<xsl:template name="release_and_links">
<div id="release-t">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</div>
<div id="release"><div>Please see <i>page source</i> for the actual XML document. </div></div>
<div id="help"><strong>
  <a href="/">HOME</a> &#183;
  <a href="/sources">DAS&#160;SOURCES</a> &#183;
  <a href="javascript:void(window.open('/help_index.jsp','help','width=700,height=550,resizable,scrollbars'))" class="blue-button">HELP</a>
</strong></div>
</xsl:template>

<xsl:template name="disclaimer">
<p class="center">
  &#169; 2007 <a href="http://www.sanger.ac.uk/" class="nowrap">Sanger Institute</a>
  </p>
</xsl:template>

<!-- support functions to split up the name... -->
<xsl:template name="first_bit">
  <xsl:param name="string" />
  <xsl:value-of select="substring-before($string,'.')" />
</xsl:template>

<xsl:template name="species_name">
  <xsl:param name="string" />
  <xsl:value-of select="translate( substring-before($string,'.'),'_',' ')" />
</xsl:template>

<xsl:template name="middle_bit">
  <xsl:param name="string" />
  <xsl:call-template name="mb">
    <xsl:with-param name="string" select="substring-after($string,'.')" />
    <xsl:with-param name="sep" />
  </xsl:call-template>
</xsl:template>

<xsl:template name="mb">
  <xsl:param name="string" />
  <xsl:param name="sep" />
  <xsl:if test="contains($string,'.')">
    <xsl:value-of select="$sep" />
    <xsl:value-of select="substring-before($string,'.')" />
    <xsl:call-template name="mb">
      <xsl:with-param name="string" select="substring-after($string,'.')" />
      <xsl:with-param name="sep">.</xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="ucfirst">
  <xsl:param name="string"/>
  <xsl:param name="strLen" select="string-length($string)"/>
  <xsl:variable name="restString" select="substring($string,2,$strLen)"/>
  <xsl:variable name="translate" select="translate(substring($string,1,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ' )" />
  <xsl:value-of select="concat($translate,$restString)"/>
</xsl:template>

<xsl:template name="last_bit">
  <xsl:param name="string" />
  <xsl:choose>
    <xsl:when test="contains($string,'.')">
      <xsl:call-template name="last_bit">
        <xsl:with-param name="string" select="substring-after($string,'.')" />
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$string" />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- DSN templates -->
<xsl:template match="DASDSN" mode="title">
  Dazzle DAS DSN
</xsl:template>

<xsl:template match="DASDSN" mode="hack"></xsl:template>

<xsl:template match="DASDSN" mode="masthead">
  <a class="section"> DAS</a><span class="viewname serif">DSN</span>
</xsl:template>

<xsl:template match="DASDSN">
<h3 class="boxed">DAS sources available</h3>
<div class="das"><table id="das" class="das"><thead>
  <tr>
    <th>#</th>
    <th>ID</th>
    <th>Species<br />Assembly</th>
    <th>Map Master<br />Notes</th>
    <th>Actions</th>
  </tr>
</thead><tbody>
  <xsl:apply-templates select="DSN"><xsl:sort select="@id" /></xsl:apply-templates>
</tbody></table></div>
</xsl:template>

<xsl:template match="DSN">
    <xsl:variable name="species"><xsl:call-template name="first_bit">
      <xsl:with-param name="string" select="SOURCE/@id" />
    </xsl:call-template></xsl:variable>
    <xsl:variable name="asm"><xsl:call-template name="middle_bit">
      <xsl:with-param name="string" select="SOURCE/@id" />
    </xsl:call-template></xsl:variable>
    <xsl:variable name="type"><xsl:call-template name="last_bit">
      <xsl:with-param name="string" select="SOURCE/@id" />
    </xsl:call-template></xsl:variable>
  <xsl:element name="tr">
    <xsl:if test="position() mod 2 = 1"><xsl:attribute name="class">alt_bg</xsl:attribute></xsl:if>
    <td class="r"><xsl:value-of select="position()"/></td>
    <td class="l"><xsl:value-of select="SOURCE/@id"/></td>
    <td class="c"><xsl:value-of select="$species" /><br /><xsl:value-of select="$asm" /></td>
    <td class="l"><xsl:value-of select="MAPMASTER"/><br /><xsl:value-of select="DESCRIPTION" /></td>
    <xsl:choose>
      <xsl:when test="$type='reference'">
    <td class="c"><xsl:element name="a"><xsl:attribute name="href" >/das/<xsl:value-of select="SOURCE/@id" />/entry_points</xsl:attribute>Entry points</xsl:element></td>
      </xsl:when>
      <xsl:otherwise>
    <td class="l"><ul>
      <li><xsl:element name="a"><xsl:attribute name="href" ><xsl:value-of select="SOURCE/@id" />/entry_points</xsl:attribute>entry_points</xsl:element></li>
    
    </ul></td>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:element>
</xsl:template>

<!-- SOURCES templates -->

<xsl:template match="SOURCES" mode="title">
  DAS Registry listing of sources
</xsl:template>
<xsl:template match="SOURCES" mode="masthead">
  <a class="section"> DAS</a><span class="viewname serif">Sources</span>
</xsl:template>

<xsl:template match="SOURCES">
<h3 class="boxed">DAS sources available</h3>
<div class="das"><table id="das" class="das"><thead>
  <tr>
    <th>#<!--<xsl:value-of select="." />--></th>
    <th>URI</th>
    <th>Title</th>
    <th>Description</th>
    <th>Species</th>
    <th>Coordinate System</th>
    <th>Maintainer</th>
    <th>Taxon ID</th>
    <th>Test range</th>
    <th>Capabilities</th>

  </tr>
</thead><tbody>
    <xsl:apply-templates select="SOURCE">
      <xsl:sort select="substring-before(@title,'.')" />
      <xsl:sort select="@uri" />
    </xsl:apply-templates>
</tbody></table></div>
</xsl:template>

<xsl:template match="SOURCE">
  <xsl:variable name="species"        select="substring-before(@title,'.')" />
  <xsl:variable name="species_name"   select="translate($species,'_',' ')" />
  
  <xsl:variable name="type">
    <xsl:call-template name="last_bit">
      <xsl:with-param name="string"  select="@title" />
    </xsl:call-template>
  </xsl:variable>

  <xsl:element name="tr">
    <xsl:choose>
      <xsl:when test="$type='reference'">
        <xsl:attribute name="class">ref_bg</xsl:attribute>
      </xsl:when>
      <xsl:when test="position() mod 2 = 1">
        <xsl:attribute name="class">alt_bg</xsl:attribute>
      </xsl:when>
    </xsl:choose>
    <td class="r"><xsl:value-of select="position()"/></td>
    <td class="l"><xsl:element name="a"><xsl:attribute name="href">http://www.dasregistry.org/showdetails.jsp?auto_id=<xsl:value-of select="@uri" /></xsl:attribute><xsl:value-of select="@uri" /></xsl:element></td>
    <td class="l"><xsl:element name="a"><xsl:attribute name="href">http://www.dasregistry.org/showdetails.jsp?auto_id=<xsl:value-of select="@uri" /></xsl:attribute><xsl:value-of select="@title" /></xsl:element></td>
    <td class="l"><xsl:value-of select="@description" /></td>
    <td class="c"><i><xsl:value-of select="$species_name" /></i></td>
    <td class="c"><xsl:element name="a"><xsl:attribute name="href"><xsl:value-of select="VERSION/COORDINATES/@uri" /></xsl:attribute><xsl:value-of select="VERSION/COORDINATES" /></xsl:element></td>
    <td class="l"><xsl:element name="a"><xsl:attribute name="href">mailto:<xsl:value-of select="MAINTAINER/@email" /></xsl:attribute><xsl:value-of select="MAINTAINER/@email" /></xsl:element></td>
    <td class="c"><xsl:element name="a"><xsl:attribute name="href">http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=<xsl:value-of select="VERSION/COORDINATES/@taxid" /></xsl:attribute><xsl:value-of select="VERSION/COORDINATES/@taxid" /></xsl:element></td>
    <td class="l"><xsl:value-of select="VERSION/COORDINATES/@test_range" /></td>
    <td class="l"><ul>
    <xsl:apply-templates select="VERSION/CAPABILITY" />
    </ul></td>

  </xsl:element>
</xsl:template>

<xsl:template match="VERSION/CAPABILITY">
  <xsl:variable name="protocol" select="substring-before(@type,':')" />
  <xsl:variable name="function" select="substring-after( @type,':')" />

      <li>
  <xsl:choose>
    <xsl:when test="($function='entry_points') or ($function='stylesheet')">
      <xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="@query_uri" /></xsl:attribute>
        <xsl:value-of select="@type" />
      </xsl:element>
    </xsl:when>
    <xsl:otherwise>
      <xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="@query_uri" />?segment=<xsl:value-of select="../COORDINATES/@test_range" /></xsl:attribute>
        <xsl:value-of select="@type" />
      </xsl:element>
    </xsl:otherwise>
  </xsl:choose>
      </li>
</xsl:template>

<!-- ENTRY_POINTS templates -->

<xsl:template match="DASEP" mode="hack"><xsl:call-template name="hack_js" /></xsl:template>

<xsl:template match="DASEP" mode="title">
  Dazzle DAS EntryPoints
</xsl:template>

<xsl:template match="DASEP" mode="masthead">
  <a class="section" id="masthead_species"><i>DAS</i></a>
  <span class="viewname serif"> Entry Points</span>
</xsl:template>

<xsl:template match="DASEP">
  <xsl:apply-templates select="ENTRY_POINTS" />
</xsl:template>

<xsl:template match="ENTRY_POINTS">
  <xsl:variable name="species"><xsl:call-template name="first_bit">
    <xsl:with-param name="string" select="substring-after( @href, '/das/' )" />
  </xsl:call-template></xsl:variable>
<h3 class="boxed">Entry points for <i>_</i></h3>
<div class="das"><table id="das" class="das"><thead>
  <tr>
    <th>#</th>
    <th>Name</th>
    <th>Type</th>
    <th>Start</th>
    <th>End</th>
    <th>Orientation</th>
    <th>-</th>
  </tr>
</thead><tbody>
  <xsl:apply-templates select="SEGMENT" mode="ep">
    <xsl:with-param name="species" select="$species" />
    <xsl:sort select="@stop" data-type="number" order="descending"/>
  </xsl:apply-templates>
</tbody></table></div>
</xsl:template>

<xsl:template match="SEGMENT" mode="ep">
  <xsl:param name="species" />
  <xsl:variable name="base" select="substring-before( @href, 'entrypoints' )" />
  <xsl:element name="tr">
    <xsl:if test="position() mod 2 = 1"><xsl:attribute name="class">alt_bg</xsl:attribute></xsl:if>
    <td class="r"><xsl:value-of select="position()"   /></td>
    <td class="l"><xsl:value-of select="@id"          /></td>
    <td class="l"><xsl:value-of select="@type"        /></td>
    <td class="r"><xsl:value-of select="@start"       /></td>
    <td class="r"><xsl:value-of select="@stop"        /></td>
    <td class="c"><xsl:value-of select="@orientation" /></td>
    <td class="c"><xsl:choose>
      <xsl:when test="@subparts">
        <xsl:element name="a">
          <xsl:attribute name="href">
            <xsl:value-of select="$base" />features?segment=<xsl:value-of select="@id" />:<xsl:value-of select="@start" />,<xsl:value-of select="@stop" />
          </xsl:attribute>
          Elements
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="a">
          <xsl:attribute name="href">
            <xsl:value-of select="$base" />features?segment=<xsl:value-of select="@id" />:<xsl:value-of select="@start" />,<xsl:value-of select="@stop" />
          </xsl:attribute>
          XXX
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose></td>
    </xsl:element>
  </xsl:template>

<!-- DNA/SEQUENCE templates -->

<xsl:template match="DASSEQUENCE" mode="hack"><xsl:call-template name="hack_js" /></xsl:template>
<xsl:template match="DASDNA"      mode="hack"><xsl:call-template name="hack_js" /></xsl:template>
<xsl:template match="DASSEQUENCE" mode="title">Dazzle Sequence</xsl:template>
<xsl:template match="DASDNA"      mode="title">Dazzle DNA Sequence</xsl:template>

<xsl:template match="DASSEQUENCE" mode="masthead">
  <a class="section" id="masthead_species"><i>DAS</i></a>
  <span class="viewname serif"> Sequence</span>
</xsl:template>

<xsl:template match="DASDNA" mode="masthead">
  <a class="section" id="masthead_species"><i>DAS</i></a>
  <span class="viewname serif"> Sequence</span>
</xsl:template>

<xsl:template match="DASSEQUENCE">
  <xsl:apply-templates select="SEQUENCE" mode="seq" />
</xsl:template>
<xsl:template match="DASDNA">
  <xsl:apply-templates select="SEQUENCE" mode="dna" />
</xsl:template>

<xsl:template match="SEQUENCE" mode="dna">
<h3 class="boxed"><i>_</i> segment: <xsl:value-of select="@id" />:<xsl:value-of select="@start" />-<xsl:value-of select="@stop" /></h3>
<pre>
  <xsl:call-template name="seq">
    <xsl:with-param name="dna"><xsl:value-of select="translate(normalize-space(DNA),' ','')" /></xsl:with-param>
    <xsl:with-param name="pos"><xsl:value-of select="@start" /></xsl:with-param>
  </xsl:call-template>
</pre>
</xsl:template>

<xsl:template match="SEQUENCE" mode="seq">
<h3 class="boxed">Segment: <xsl:value-of select="@id" />:<xsl:value-of select="@start" />-<xsl:value-of select="@stop" /></h3>
<pre>
  <xsl:call-template name="seq">
    <xsl:with-param name="dna"><xsl:value-of select="translate(normalize-space(.),' ','')" /></xsl:with-param>
    <xsl:with-param name="pos"><xsl:value-of select="@start" /></xsl:with-param>
  </xsl:call-template>
</pre>
</xsl:template>

<xsl:template name="seq">
  <xsl:param name="dna" />
  <xsl:param name="pos" />
  <xsl:choose>
    <xsl:when test="string-length($dna) &gt; 6000000">
      <xsl:call-template name="seq">
        <xsl:with-param name="dna" select="substring($dna,1,6000000)" />
        <xsl:with-param name="pos" select="$pos" />
      </xsl:call-template>
      <xsl:call-template name="seq">
        <xsl:with-param name="dna" select="substring($dna,6000001)" />
        <xsl:with-param name="pos" select="$pos + 6000000" />
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="string-length($dna) &gt; 6000">
      <xsl:call-template name="seq">
        <xsl:with-param name="dna" select="substring($dna,1,6000)" />
        <xsl:with-param name="pos" select="$pos" />
      </xsl:call-template>
      <xsl:call-template name="seq">
        <xsl:with-param name="dna" select="substring($dna,6001)" />
        <xsl:with-param name="pos" select="$pos + 6000" />
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="string-length($dna) &gt; 60">
      <xsl:call-template name="seq">
        <xsl:with-param name="dna" select="substring($dna,1,60)" />
        <xsl:with-param name="pos" select="$pos" />
      </xsl:call-template>
      <xsl:call-template name="seq">
        <xsl:with-param name="dna" select="substring($dna,61)" />
        <xsl:with-param name="pos" select="$pos + 60" />
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="$dna != ''">
      <xsl:variable name="rpos"      select="$pos + string-length($dna) -1" />
      <xsl:value-of select="concat( substring(concat('          ',$pos),string-length(string($pos))), ' ')" />
      <xsl:value-of select="substring(concat( $dna,'                                                                                                    '),1,60)" />
      <xsl:value-of select="substring(concat('           ',$rpos),string-length(string($rpos)))" />
    <br />
    </xsl:when>
  </xsl:choose>
</xsl:template>

<!-- GFF templates -->

<xsl:template match="DASTYPES" mode="hack"></xsl:template>
<xsl:template match="DASTYPES" mode="title">
  Dazzle <xsl:call-template name="species_name">
    <xsl:with-param name="string" select="substring-after( GFF/@href, '/das/' )" />
  </xsl:call-template>&#160;<xsl:call-template name="last_bit">
    <xsl:with-param name="string" select="substring-before( GFF/@href, '/types?' )" />
  </xsl:call-template> Feature types
</xsl:template>

<xsl:template match="DASTYPES" mode="masthead">
  <xsl:variable name="species"><xsl:call-template name="first_bit">
    <xsl:with-param name="string" select="substring-after( GFF/@href, '/das/' )" />
  </xsl:call-template></xsl:variable>
  <xsl:variable name="type"><xsl:call-template name="last_bit">
    <xsl:with-param name="string" select="substring-before( GFF/@href, '/types?' )" />
  </xsl:call-template></xsl:variable>
  <xsl:element name="a">
    <xsl:attribute name="class">section</xsl:attribute>
    <xsl:attribute name="href" >/<xsl:value-of select="$species" />/</xsl:attribute>
    <i><xsl:value-of select="translate($species,'_',' ')" /></i>
  </xsl:element>
  <span class="viewname serif">&#160;<xsl:call-template name="ucfirst"><xsl:with-param name="string" select="$type" /></xsl:call-template>&#160;feature types</span>
</xsl:template>

<xsl:template match="DASTYPES">
  <xsl:apply-templates select="GFF" mode="type"/>
</xsl:template>

<xsl:template match="DASGFF" mode="hack"></xsl:template>
<xsl:template match="DASGFF" mode="title">
  Dazzle <xsl:call-template name="species_name">
    <xsl:with-param name="string" select="substring-after( GFF/@href, '/das/' )" />
  </xsl:call-template>&#160;<xsl:call-template name="last_bit">
    <xsl:with-param name="string" select="substring-before( GFF/@href, '/features?' )" />
  </xsl:call-template>
</xsl:template>

<xsl:template match="DASGFF" mode="masthead">
  <xsl:variable name="species"><xsl:call-template name="first_bit">
    <xsl:with-param name="string" select="substring-after( GFF/@href, '/das/' )" />
  </xsl:call-template></xsl:variable>
  <xsl:variable name="type"><xsl:call-template name="last_bit">
    <xsl:with-param name="string" select="substring-before( GFF/@href, '/features?' )" />
  </xsl:call-template></xsl:variable>
  <xsl:element name="a">
    <xsl:attribute name="class">section</xsl:attribute>
    <xsl:attribute name="href" >/<xsl:value-of select="$species" />/</xsl:attribute>
    <i><xsl:value-of select="translate($species,'_',' ')" /></i>
  </xsl:element>
  <span class="viewname serif"> <xsl:call-template name="ucfirst"><xsl:with-param name="string" select="$type" /></xsl:call-template>&#160;features</span>
</xsl:template>

<xsl:template match="DASGFF">
  <xsl:apply-templates select="GFF" />
</xsl:template>

<xsl:template match="GFF" mode="type">
  <xsl:variable name="species"><xsl:call-template name="first_bit">
    <xsl:with-param name="string" select="substring-after( @href, '/das/' )" />
  </xsl:call-template></xsl:variable>
  <xsl:variable name="type"><xsl:call-template name="last_bit">
    <xsl:with-param name="string" select="substring-before(substring-after( @href, '/das/' ),'/')" />
  </xsl:call-template></xsl:variable>

<h3 class="boxed">Das types from: <xsl:value-of select="@href"/></h3>
<div class="das"><table id="das" class="das">
  <xsl:apply-templates select="SEGMENT|ERRORSEGMENT|UNKNOWNSEGMENT" mode="type">
    <xsl:with-param name="species" select="$species" />
    <xsl:with-param name="type" select="$type" />
    <xsl:sort select="@id" />
  </xsl:apply-templates>
</table></div>
</xsl:template>

<xsl:template match="GFF">
  <xsl:variable name="species"><xsl:call-template name="first_bit">
    <xsl:with-param name="string" select="substring-after( @href, '/das/' )" />
  </xsl:call-template></xsl:variable>
  <xsl:variable name="type"><xsl:call-template name="last_bit">
    <xsl:with-param name="string" select="substring-before(substring-after( @href, '/das/' ),'/')" />
  </xsl:call-template></xsl:variable>

<h3 class="boxed">Das Features from: <xsl:value-of select="@href"/></h3>
<div class="das"><table id="das" class="das">
  <xsl:apply-templates select="SEGMENT|ERRORSEGMENT|UNKNOWNSEGMENT" mode="gff">
    <xsl:with-param name="species" select="$species" />
    <xsl:with-param name="type" select="$type" />
    <xsl:sort select="@id" />
  </xsl:apply-templates>
</table></div>
</xsl:template>

<xsl:template match="UNKNOWNSEGMENT" mode="type">
  <xsl:param name="species" />
  <xsl:param name="type" />
  <tbody class="head">
  <tr>
    <th colspan="5">Segment: <xsl:value-of select="@id" />:<xsl:value-of select="@start" />-<xsl:value-of select="@stop" /></th>
  </tr>
  <tr>
    <td colspan="5" class="c e">Unknown segment</td>
  </tr>
  </tbody>
</xsl:template>

<xsl:template match="ERRORSEGMENT" mode="type">
  <xsl:param name="species" />
  <xsl:param name="type" />
  <tbody class="head">
  <tr>
    <th colspan="5">Segment: <xsl:value-of select="@id" />:<xsl:value-of select="@start" />-<xsl:value-of select="@stop" /></th>
  </tr>
  <tr>
    <td colspan="5" class="e c">Error in selected segment (region chosen outside range of <xsl:value-of select="@id" />)</td>
  </tr>
  </tbody>
</xsl:template>

<xsl:template match="SEGMENT" mode="type">
  <xsl:param name="species" />
  <xsl:param name="type" />
  <tbody class="head">
  <xsl:if test="@id">
  <tr>
    <th colspan="6">Segment: <xsl:value-of select="@id" />:<xsl:value-of select="@start" />-<xsl:value-of select="@stop" />
    []
    </th>
  </tr>
  </xsl:if>
  <xsl:choose>
    <xsl:when test="TYPE">
    <tr>
    <th>#</th>
    <th>Category</th>
    <th>Type</th>
    <th>Method</th>
    <th>Count</th>
  </tr>
    </xsl:when>
    <xsl:otherwise>
  <tr><td colspan="11" class="c">No features on this segment</td></tr>
    </xsl:otherwise>
  </xsl:choose>
  </tbody>
  <tbody>
  <xsl:apply-templates select="TYPE" mode="gff">
    <xsl:sort select="@category" />
    <xsl:sort select="@id" />
    <xsl:with-param name="species" select="$species" />
    <xsl:with-param name="type" select="$type" />
  </xsl:apply-templates>
  </tbody>
</xsl:template>

<xsl:template match="UNKNOWNSEGMENT" mode="gff">
  <xsl:param name="species" />
  <xsl:param name="type" />
  <tbody class="head">
  <tr>
    <th colspan="11">Segment: <xsl:value-of select="@id" />:<xsl:value-of select="@start" />-<xsl:value-of select="@stop" /></th>
  </tr>
  <tr>
    <td colspan="11" class="c e">Unknown segment</td>
  </tr>
  </tbody>
</xsl:template>

<xsl:template match="ERRORSEGMENT" mode="gff">
  <xsl:param name="species" />
  <xsl:param name="type" />
  <tbody class="head">
  <tr>
    <th colspan="11">Segment: <xsl:value-of select="@id" />:<xsl:value-of select="@start" />-<xsl:value-of select="@stop" /></th>
  </tr>
  <tr>
    <td colspan="11" class="e c">Error in selected segment (region chosen outside range of <xsl:value-of select="@id" />)</td>
  </tr>
  </tbody>
</xsl:template>

<xsl:template match="SEGMENT" mode="gff">
  <xsl:param name="species" />
  <xsl:param name="type" />
  <tbody class="head">
  <tr>
    <th colspan="11">Segment: <xsl:value-of select="@id" />:<xsl:value-of select="@start" />-<xsl:value-of select="@stop" />
    []
    </th>
  </tr>
  <xsl:choose>
    <xsl:when test="FEATURE">
  <tr>
    <th>#</th>
    <th>Label<br />(Grouping)</th>
    <th>Type<br />Category</th>
    <th>Method</th>
    <th>Start</th>
    <th>End</th>
    <th>Orientation</th>
    <th>Score</th>
    <th>Target</th>
    <th>Links</th>
    <th>Notes</th>
  </tr>
    </xsl:when>
    <xsl:otherwise>
  <tr><td colspan="11" class="c">No features on this segment</td></tr>
    </xsl:otherwise>
  </xsl:choose>
  </tbody>
  <tbody>
  <xsl:apply-templates select="FEATURE">
    <xsl:sort select="TYPE/@id" />
    <xsl:sort select="START" data-type="number" />
    <xsl:with-param name="species" select="$species" />
    <xsl:with-param name="type" select="$type" />
  </xsl:apply-templates>
  </tbody>
</xsl:template>

<xsl:template match="TYPE" mode="gff">
  <xsl:param name="species" />
  <xsl:param name="type" />
  <xsl:variable name="base_URL" select="substring-before(../../@href,'/features?')" />
  <xsl:element name="tr">
  <xsl:if test="position() mod 2 = 1"><xsl:attribute name="style">background-color: #f0f0f0</xsl:attribute></xsl:if>
    <td class="r"><xsl:value-of select="position()" /></td>
    <td class="c"><xsl:value-of select="@category" /></td>
    <td class="c"><xsl:value-of select="@id" /></td>
    <td class="c"><xsl:value-of select="@method" /></td>
    <td class="r"><xsl:value-of select="." /></td>
  </xsl:element>
</xsl:template>

<xsl:template match="GROUP" mode="gff">
  <xsl:param name="base_URL" />
  <li><xsl:element name="a">
    <xsl:attribute name="href">
      <xsl:value-of select="$base_URL" />/features?group_id=<xsl:value-of select="@id" />
    </xsl:attribute><xsl:choose>
    <xsl:when test="@label">
      <xsl:value-of select="@label" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="@id" />
    </xsl:otherwise>
    </xsl:choose>
  </xsl:element></li>
</xsl:template>

<xsl:template match="FEATURE">
  <xsl:param name="species" />
  <xsl:param name="type" />
  <xsl:variable name="base_URL" select="substring-before(../../@href,'/features?')" />
  <xsl:element name="tr">
  <xsl:if test="position() mod 2 = 1"><xsl:attribute name="style">background-color: #f0f0f0</xsl:attribute></xsl:if>
    <td class="r"><xsl:value-of select="position()" /></td>
    <td class="l"><ul>
      <li><xsl:element name="a">
        <xsl:attribute name="href">
          <xsl:value-of select="$base_URL" />/features?feature_id=<xsl:value-of select="@id" />
        </xsl:attribute><xsl:choose>
        <xsl:when test="@label">
          <xsl:value-of select="@label" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@id" />
        </xsl:otherwise>
        </xsl:choose>
      </xsl:element></li>
      <xsl:apply-templates select="GROUP" mode="gff">
        <xsl:with-param name="base_URL" select="$base_URL" />
        <xsl:sort select="@id" />
      </xsl:apply-templates>
    </ul></td>
    <td class="c"><xsl:value-of select="TYPE"       /><br /><xsl:value-of select="TYPE/@category" /></td>
    <td class="c"><xsl:value-of select="METHOD"     /></td>
    <td class="r"><xsl:value-of select="START"      /></td>
    <td class="r"><xsl:value-of select="END"        /></td>
    <td class="c"><xsl:value-of select="ORIENTATION"/></td>
    <td class="c"><xsl:value-of select="SCORE"      /></td>
    <td class="c"><xsl:choose>
      <xsl:when test="TARGET">
        <xsl:value-of select="TARGET/@id" /><br />
        <xsl:value-of select="TARGET/@start" />-<xsl:value-of select="TARGET/@stop" />
      </xsl:when>
      <xsl:otherwise>
        -
      </xsl:otherwise>
    </xsl:choose></td>
    <td class="l"><ul>
    <xsl:if test="TYPE/@reference='yes'">
      <xsl:if test="TYPE/@subparts='yes'">
        <li><xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="$base_URL" />/features?segment=<xsl:value-of select="@id" /></xsl:attribute>
          <em>DAS</em> Assembly
        </xsl:element></li>
      </xsl:if>
      <li><xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="$base_URL" />/dna?segment=<xsl:value-of select="@id" /></xsl:attribute>
          <em>DAS</em> Sequence
      </xsl:element></li>
    </xsl:if>
      <li></li>
    <xsl:apply-templates select="LINK"/>
    <xsl:apply-templates select="GROUP/LINK"/>
    </ul></td>
    <td class="l">
      <xsl:if test="(NOTE) or (GROUP/NOTE)"><ul>
        <xsl:apply-templates select="NOTE"/>
        <xsl:apply-templates select="GROUP/NOTE"/>
      </ul></xsl:if></td>
  </xsl:element>
</xsl:template>

<xsl:template match="LINK">
  <xsl:choose>
    <xsl:when test="(@href) and (@href!='')">
      <li><xsl:element name="a">
        <xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
        <xsl:value-of select="."/>
      </xsl:element></li>
    </xsl:when>
    <xsl:otherwise>
      <li><span class="nb"><xsl:value-of select="." /></span></li>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="NOTE">
      <li><small><xsl:value-of select="."/></small></li>
</xsl:template>

<!-- LAST BUT NOT LEAST STYLESHEETS -->

<xsl:template match="DASSTYLE" mode="title">
  Dazzle DAS StyleSheet
</xsl:template>

<xsl:template match="DASSTYLE" mode="masthead">
  <xsl:element name="a">
    <xsl:attribute name="class">section</xsl:attribute>
    DAS
  </xsl:element>
  <span class="viewname serif"> StyleSheet</span>
</xsl:template>

<xsl:template match="DASSTYLE">
  <xsl:apply-templates select="STYLESHEET" />
</xsl:template>

<xsl:template match="STYLESHEET">
<h3 class="boxed">Feature styles...</h3>
<div class="das"><table id="das" class="das">
<tr>
  <th>#</th>
  <th>Category</th>
  <th>Type</th>
  <th>Glyph</th>
</tr>
  <xsl:apply-templates select="CATEGORY/TYPE" mode="ss">
    <xsl:sort select="../@id" />
    <xsl:sort select="@id" />
  </xsl:apply-templates>
</table></div>
</xsl:template>
<xsl:template match="CATEGORY/TYPE" mode="ss">
  <xsl:variable name="alt" select="position() mod 2 = 1" />
  <xsl:element name="tr">
    <xsl:if test="$alt"><xsl:attribute name="class">alt_bg</xsl:attribute></xsl:if>
    <xsl:element name="td">
      <xsl:attribute name="class">r</xsl:attribute>
      <xsl:attribute name="rowspan"><xsl:value-of select="count(GLYPH/*)" /></xsl:attribute>
      <xsl:value-of select="position()" />
    </xsl:element>
    <xsl:element name="td">
      <xsl:attribute name="class">c</xsl:attribute>
      <xsl:attribute name="rowspan"><xsl:value-of select="count(GLYPH/*)" /></xsl:attribute>
      <xsl:value-of select="../@id" />
    </xsl:element>
    <xsl:element name="td">
      <xsl:attribute name="class">c</xsl:attribute>
      <xsl:attribute name="rowspan"><xsl:value-of select="count(GLYPH/*)" /></xsl:attribute>
      <xsl:value-of select="@id" />
    </xsl:element>
    <xsl:apply-templates select="GLYPH[position()=1]/*[position()=1]" mode="ss" />
  </xsl:element>
  <xsl:apply-templates select="GLYPH[position()=1]/*[position()!=1]" mode="ss-tr"><xsl:with-param name="alt" select="$alt" /></xsl:apply-templates>
  <xsl:apply-templates select="GLYPH[position()!=1]/*" mode="ss-tr"><xsl:with-param name="alt" select="$alt" /></xsl:apply-templates>
</xsl:template>

<xsl:template match="GLYPH/*" mode="ss-tr">
  <xsl:param name="alt" />
  <xsl:element name="tr"><xsl:if test="$alt"><xsl:attribute name="class">alt_bg</xsl:attribute></xsl:if>
  <xsl:apply-templates select="." mode="ss"/>
  </xsl:element>
</xsl:template>

<xsl:template match="GLYPH/*" mode="ss">
  <td class="l"><strong><xsl:value-of select="name(.)" />: </strong> <xsl:apply-templates select="." mode="ss-attr" />
  </td>
</xsl:template>
<xsl:template match="*" mode="ss-attr">
  <xsl:for-each select="*">
    <xsl:sort select="name(.)" />
    <xsl:value-of select="name(.)" /> = <xsl:value-of select="." />;
  </xsl:for-each>
</xsl:template>
</xsl:stylesheet>

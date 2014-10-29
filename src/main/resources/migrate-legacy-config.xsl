<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml"/>
	<xsl:template match="/">
		<rootConfig>
			<plugins>
				<xsl:apply-templates/>
			</plugins>
		</rootConfig>
	</xsl:template>
	<xsl:template match="/rootConfig/plugins">
		<plugin>
			<xsl:copy-of select="*"/>
		</plugin>
	</xsl:template>
</xsl:stylesheet>

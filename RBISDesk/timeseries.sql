SELECT stat_ts.ts_timeser_id as "stat_ts.ts_timeser_id",mdconst.md_metadata_id as "mdconst.md_metadata_id",mdcontact.md_metadata_id as "mdcontact.md_metadata_id",dsstation.dsstation_id as "dsstation.dsstation_id",dsstation.statname as "dsstation.statname",dsstation.latutm as "dsstation.latutm",dsstation.longutm as "dsstation.longutm",dsstation.elevat as "dsstation.elevat",dsstation.yearestabl as "dsstation.yearestabl",dsstation.yearclosed as "dsstation.yearclosed",dsstation.reprarea as "dsstation.reprarea",dsstation.statdesc as "dsstation.statdesc",dsstation.disttomouth as "dsstation.disttomouth",map_srid.name as "map_srid.name",riverseg.rivername as "riverseg.rivername",respparty.rporgname as "respparty.rporgname",respparty.rpindname as "respparty.rpindname",consts.uselimit as "consts.uselimit",ts_timeser.ts_timeser_id as "ts_timeser.ts_timeser_id",ts_timeser.md_metadata_id as "ts_timeser.md_metadata_id",ts_timeser.dr_datarelia_id as "ts_timeser.dr_datarelia_id",ts_timeser.missing_value as "ts_timeser.missing_value",ts_timeser.startdatetime as "ts_timeser.startdatetime",ts_timeser.enddatetime as "ts_timeser.enddatetime",respparty2.rporgname as "respparty2.rporgname",respparty2.rpindname as "respparty2.rpindname",metadata.md_metadata_id as "metadata.md_metadata_id",metadata.md_distrib_id as "metadata.md_distrib_id",metadata.mddatest as "metadata.mddatest",distributors.md_distributor_id as "distributors.md_distributor_id",distributors.md_distrib_id as "distributors.md_distrib_id",distrib.md_distrib_id as "distrib.md_distrib_id",distrib.auxiliary_col as "distrib.auxiliary_col",respparty3.rporgname as "respparty3.rporgname",respparty3.rpindname as "respparty3.rpindname",distributor.md_distributor_id as "distributor.md_distributor_id",dataidinfo.md_metadata_id as "dataidinfo.md_metadata_id",dataidinfo.md_ident_id as "dataidinfo.md_ident_id",ident.md_ident_id as "ident.md_ident_id",ident.ci_citation_id as "ident.ci_citation_id",ident.idabs as "ident.idabs",citation.ci_citation_id as "citation.ci_citation_id",citation.restitle as "citation.restitle",uomunits.unit as "uomunits.unit",dr_datarelia.dr_datarelia_id as "dr_datarelia.dr_datarelia_id",dr_datarelia.dr_descr as "dr_datarelia.dr_descr",scstatuscd.status as "scstatuscd.status",ts_kind.ts_kind as "ts_kind.ts_kind", 1, 1, ts_timeser.md_metadata_id as "ts_timeser.md_metadata_id" 
FROM ts_timeser 
LEFT JOIN metadata ON ts_timeser.md_metadata_id = metadata.md_metadata_id 
LEFT JOIN dataidinfo ON metadata.md_metadata_id = dataidinfo.md_metadata_id 
LEFT JOIN ident ON dataidinfo.md_ident_id = ident.md_ident_id 
LEFT JOIN citation ON ident.ci_citation_id = citation.ci_citation_id 
LEFT JOIN stat_ts ON ts_timeser.ts_timeser_id = stat_ts.ts_timeser_id 
LEFT JOIN dr_datarelia ON ts_timeser.dr_datarelia_id = dr_datarelia.dr_datarelia_id 
LEFT JOIN scstatuscd ON dr_datarelia.sc_scstatuscd_id = scstatuscd.sc_scstatuscd_id 
LEFT JOIN ts_kind ON ts_timeser.ts_kind_id = ts_kind.ts_kind_id 
LEFT JOIN dsstation ON stat_ts.dsstation_id = dsstation.dsstation_id 
LEFT JOIN riverseg ON dsstation.rivsyst_id = riverseg.rivsyst_id 
LEFT JOIN mdcontact ON metadata.md_metadata_id = mdcontact.md_metadata_id 
LEFT JOIN respparty ON mdcontact.ci_respparty_id = respparty.ci_respparty_id 
LEFT JOIN mdconst ON metadata.md_metadata_id = mdconst.md_metadata_id 
LEFT JOIN consts ON mdconst.md_consts_id = consts.md_consts_id 
LEFT JOIN respparty respparty2 ON dsstation.ci_respparty_id = respparty2.ci_respparty_id 
LEFT JOIN distrib ON metadata.md_distrib_id = distrib.md_distrib_id 
LEFT JOIN distributors ON distrib.md_distrib_id = distributors.md_distrib_id 
LEFT JOIN distributor ON distributors.md_distributor_id = distributor.md_distributor_id 
LEFT JOIN respparty respparty3 ON distributor.ci_respparty_id = respparty3.ci_respparty_id 
LEFT JOIN uomunits ON ts_timeser.uom_units_id = uomunits.uom_units_id 
LEFT JOIN map_srid ON dsstation.srid = map_srid.srid 
WHERE ts_timeser.md_metadata_id!= 0
ORDER BY ts_timeser.md_metadata_id LIMIT 20 


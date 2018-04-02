package com.smartlivedata.procedures;

import com.smartlivedata.osm.data.OSMEntity;
import com.smartlivedata.osm.parser.OSMParser;
import com.smartlivedata.osm.writer.FastWriter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.QueryStatistics;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.stream.Stream;

import static org.neo4j.procedure.Mode.WRITE;

public class AtlasProcedures {

    @Context
    public GraphDatabaseService db;
    @Context
    public Log log;

    @Procedure(mode = Mode.WRITE)
    @Description("com.smartlivedata.procedures.importOSM() | Creates the sample movies graph")
    public Stream<ProcedureOutput> importOSM(@Name("path") String path) {
        ProcedureOutput procedureOutput = new ProcedureOutput();
        OSMParser osmParser = new OSMParser();
        try {
            FastWriter fastWriter = new FastWriter();
            fastWriter.start(db);
            osmParser.getEntityStream(path).filter(t -> t instanceof OSMEntity).map(t -> (OSMEntity)t).forEach(osmEntity -> fastWriter.addOSMEntity(osmEntity));
        }
        catch (XMLStreamException xmlStreamException) {
            procedureOutput.importedSuccessfully = false;
            procedureOutput.message = "XML malformed";
            return Stream.of(procedureOutput);
        }
        catch (FileNotFoundException fileNotFoundException) {
            procedureOutput.importedSuccessfully = false;
            procedureOutput.message = "File not found!";
            return Stream.of(procedureOutput);
        }
        return Stream.of(procedureOutput);
    }

}

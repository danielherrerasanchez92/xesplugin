/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package org.pentaho.di.sdk.samples.steps.demo;

import com.sun.xml.internal.fastinfoset.util.CharArray;
import javassist.bytecode.ByteArray;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.*;
import org.deckfour.xes.model.impl.*;
import org.deckfour.xes.out.XesXmlSerializer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.syslog.SyslogMessage;

import java.io.FileOutputStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI.
 * <p>
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 * <p>
 * This class is the implementation of StepInterface.
 * Classes implementing this interface need to:
 * <p>
 * - initialize the step
 * - execute the row processing logic
 * - dispose of the step
 * <p>
 * Please do not create any local fields in a StepInterface class. Store any
 * information related to the processing logic in the supplied step data interface
 * instead.
 */

public class DemoStep extends BaseStep implements StepInterface {

    //global attributes
    private XLogImpl log;
    private XExtensionManager xExtensionManager;
    private Map<String, String> mapa_columnas;
    private LinkedList<XTraceImpl> lista_traces;
    private Map<String, String> mapa_IPRegistradas;
    private XAttributeMapImpl mapaGlobalAtributos;
    private Map<String, Boolean> mapaUsoAtributos;

    /**
     * The constructor should simply pass on its arguments to the parent class.
     *
     * @param s                 step description
     * @param stepDataInterface step data class
     * @param c                 step copy
     * @param t                 transformation description
     * @param dis               transformation executing
     */
    public DemoStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
        super(s, stepDataInterface, c, t, dis);
    }

    /**
     * This method is called by PDI during transformation startup.
     * <p>
     * It should initialize required for step execution.
     * <p>
     * The meta and data implementations passed in can safely be cast
     * to the step's respective implementations.
     * <p>
     * It is mandatory that super.init() is called to ensure correct behavior.
     * <p>
     * Typical tasks executed here are establishing the connection to a database,
     * as wall as obtaining resources, like file handles.
     *
     * @param smi step meta interface implementation, containing the step settings
     * @param sdi step data interface implementation, used to store runtime information
     * @return true if initialization completed successfully, false if there was an error preventing the step from working.
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        // Casting to step-specific implementation classes is safe
        DemoStepMeta meta = (DemoStepMeta) smi;
        DemoStepData data = (DemoStepData) sdi;

        //my initializations
        XAttributeMapImpl atb_map = new XAttributeMapImpl();
        this.log = new XLogImpl(atb_map);
        this.xExtensionManager = XExtensionManager.instance();
        this.lista_traces = new LinkedList<XTraceImpl>();
        this.mapa_columnas = new HashMap<String, String>();
        this.mapa_IPRegistradas = new HashMap<String, String>();
        this.mapaGlobalAtributos = new XAttributeMapImpl();
        this.mapaUsoAtributos = new HashMap<>();

        IniciarMapaGlobal();

        return super.init(meta, data);
    }

    private void IniciarMapaGlobal() {
        this.mapaGlobalAtributos.put("conceptkey", new XAttributeLiteralImpl("concept:name", ""));
        this.mapaGlobalAtributos.put("timestampkey", new XAttributeTimestampImpl("time:timestamp", new java.util.Date("01/01/1970 00:00:00")));
        this.mapaGlobalAtributos.put("lifecyclekey", new XAttributeLiteralImpl("lifecycle:transition", "complete"));
        this.mapaGlobalAtributos.put("resourcekey", new XAttributeLiteralImpl("org:resource", ""));
        this.mapaGlobalAtributos.put("rolekey", new XAttributeLiteralImpl("org:role", ""));
        this.mapaGlobalAtributos.put("groupkey", new XAttributeLiteralImpl("org:group", ""));
    }

    /**
     * Once the transformation starts executing, the processRow() method is called repeatedly
     * by PDI for as long as it returns true. To indicate that a step has finished processing rows
     * this method must call setOutputDone() and return false;
     * <p>
     * Steps which process incoming rows typically call getRow() to read a single row from the
     * input stream, change or add row content, call putRow() to pass the changed row on
     * and return true. If getRow() returns null, no more rows are expected to come in,
     * and the processRow() implementation calls setOutputDone() and returns false to
     * indicate that it is done too.
     * <p>
     * Steps which generate rows typically construct a new row Object[] using a call to
     * RowDataUtil.allocateRowData(numberOfFields), add row content, and call putRow() to
     * pass the new row on. Above process may happen in a loop to generate multiple rows,
     * at the end of which processRow() would call setOutputDone() and return false;
     *
     * @param smi the step meta interface containing the step settings
     * @param sdi the step data interface that should be used to store
     * @return true to indicate that the function should be called again, false if the step is done
     */
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

        // safely cast the step settings (meta) and runtime info (data) to specific implementations
        DemoStepMeta meta = (DemoStepMeta) smi;
        DemoStepData data = (DemoStepData) sdi;

        // get incoming row, getRow() potentially blocks waiting for more rows, returns null if no more rows expected
        Object[] r = getRow();

        // if no more rows are expected, indicate step is finished and processRow() should not be called again
        if (r == null) {

            setOutputDone();

            //revisando que atributos se han usado, para ponerlos como atributos globales y poner tambien los <extension>
            UsoAtributos();
            //se serializa una vez que se han convertido todos los datos
            XesXmlSerializer serializer = new XesXmlSerializer();
            try {
                this.log.addAll(this.lista_traces);

                //trabajando con las rutas de salida
                String ruta = this.mapa_columnas.get("RutaSalida");
                if (ruta == null || ruta.equalsIgnoreCase("")) {
                    serializer.serialize(this.log, new FileOutputStream("output.xes"));
                } else {
                    serializer.serialize(this.log, new FileOutputStream(ruta));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        // the "first" flag is inherited from the base step implementation
        // it is used to guard some processing tasks, like figuring out field indexes
        // in the row structure that only need to be done once
        if (first) {
            first = false;
            // clone the input row structure and place it in our data object
            data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
        }

        // XES coding starting here
        // Mapeo hecho en metodo init().

        String nombresColumnas[] = data.outputRowMeta.getFieldNames();

        //Creo el evento
        Map<String, XAttribute> map = new XAttributeMapImpl(); //un mapa para llenarlo con los datos del evento
        XEventImpl event = new XEventImpl(new XAttributeMapImpl()); //el evento

        //Inicializando variables que seran empleadas en el "desguace" por columnas
        String valorIP = "";
        String line = "";
        String valor_actividad = "";
        String valor_cicloVida = "";
        String valor_recurso = "";
        String valor_rol = "";
        String valor_grupo = "";

        //asignandole a mapa columnas el valor q viene desde la vista:
        this.mapa_columnas = meta.getMapa_vista();

        for (int i = 0; i < nombresColumnas.length; i++) {
            //en dependencia del dato q sea adicionarlo al map como el tipo de atributo q es
            if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("InstanciaProceso"))) {
                valorIP = DescifrarPalabra(r[i]);
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("MarcaTiempo"))) {
                line = DescifrarPalabra(r[i]);
                this.mapaUsoAtributos.put("timestamp", true);
                if (line.isEmpty()) {
                    //si lo q entra por la linea de timestamp es nulo, se pone este que viene por defecto
                    //todo esto lo debo poner desde la lista de globales, o similar, no directamente aki
                    map.put("DateKey", new XAttributeTimestampImpl("time:timestamp", new java.util.Date("01/01/1970 00:00:00")));
                } else {
                    try {
                        Date date = new SimpleDateFormat(this.mapa_columnas.get("RegexMarcaTiempo")).parse(line);
                        map.put("DateKey", new XAttributeTimestampImpl("time:timestamp", date));
                    } catch (Exception e) {
                    }
                }

            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("Actividad"))) {
                valor_actividad = DescifrarPalabra(r[i]);
                map.put("ActividadKey", new XAttributeLiteralImpl("concept:name", valor_actividad));
                this.mapaUsoAtributos.put("concept", true);
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("CicloVida"))) {
                valor_cicloVida = DescifrarPalabra(r[i]);
                map.put("CicloVidaKey", new XAttributeLiteralImpl("lifecycle:transition", valor_cicloVida));
                this.mapaUsoAtributos.put("lifecycle", true);
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("Recurso"))) {
                valor_recurso = DescifrarPalabra(r[i]);
                map.put("RecursoKey", new XAttributeLiteralImpl("org:resource", valor_recurso));
                this.mapaUsoAtributos.put("resource", true);
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("Rol"))) {
                valor_rol = DescifrarPalabra(r[i]);
                map.put("RolKey", new XAttributeLiteralImpl("org:role", valor_rol));
                this.mapaUsoAtributos.put("role", true);
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("Grupo"))) {
                valor_grupo = DescifrarPalabra(r[i]);
                map.put("GrupoKey", new XAttributeLiteralImpl("org:group", valor_grupo));
                this.mapaUsoAtributos.put("group", true);
            }
        }

        //vinculo el mapa con datos del evento al evento en si
        event.setAttributes(new XAttributeMapImpl(map));

        // para ir adicionando el evento
        String pos_lista_traces = mapa_IPRegistradas.get(valorIP);

        if (pos_lista_traces == null) {
            //Pseudo-codigo:: ;)
            //quiere decir q no se ha tratado nunca con este trace
            //se crea un nuevo trace, se vincula con el event, y se almacena en la lista d traces (al final)
            //actualizar mapaIPRegistradas
            ////// crear en mapaIPRegistradas una nueva entrada con llave valorIP, y valor mapa.size()

            //poniendo atributos al trace
            Map<String, XAttribute> map_trace = new XAttributeMapImpl();
            map_trace.put("IPKey", new XAttributeLiteralImpl("concept:name", valorIP));
            //adicionando la extension utilizada al log
            XExtension extension = xExtensionManager.getByPrefix("concept");
            this.log.getExtensions().add(extension);

            XTraceImpl trace = new XTraceImpl(new XAttributeMapImpl());
            trace.setAttributes(new XAttributeMapImpl(map_trace));
            trace.add(event);
            this.lista_traces.add(trace);

            //actualizando mapaIPRegistradas
            this.mapa_IPRegistradas.put(valorIP, String.valueOf(mapa_IPRegistradas.size())); //=mente pudiera ser size de lista_traces


        } else {
            //Pseudo-codigo:: ;)
            //"pos" tiene la posicion de lista de traces en la q se trata a esa IP (pos es "String")
            // se vincula el event con el trace q esta en esa posicion devuelta

            this.lista_traces.get(Integer.parseInt(pos_lista_traces)).add(event);
        }


        //////////////DE AKI PA'BAJO EL MISMO COMPORTAMIENTO DEL DUMMY-PLUGIN////////////////////////////

        // log progress if it is time to to so
        if (checkFeedback(getLinesRead())) {
            logBasic("Linenr " + getLinesRead()); // Some basic logging
        }

        // indicate that processRow() should be called again
        return true;
    }

    /**
     * Metodos auxiliares desarrollados
     */

    private String DescifrarPalabra(Object arr) {
        if (arr instanceof byte[]) { //works !
            byte[] input = ((byte[]) arr);
            return new String(input);
        }
        return String.valueOf(arr);
    }

    private void UsoAtributos() {
        if (this.mapaUsoAtributos.get("concept") != null) {
            this.log.getGlobalEventAttributes().add(this.mapaGlobalAtributos.get("conceptkey"));
            XExtension extension = xExtensionManager.getByPrefix("concept");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("lifecycle") != null) {
            this.log.getGlobalEventAttributes().add(this.mapaGlobalAtributos.get("lifecyclekey"));
            XExtension extension = xExtensionManager.getByPrefix("lifecycle");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("timestamp") != null) {
            this.log.getGlobalEventAttributes().add(this.mapaGlobalAtributos.get("timestampkey"));
            XExtension extension = xExtensionManager.getByPrefix("time");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("resource") != null) {
            this.log.getGlobalEventAttributes().add(this.mapaGlobalAtributos.get("resourcekey"));
            XExtension extension = xExtensionManager.getByPrefix("org");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("role") != null) {
            this.log.getGlobalEventAttributes().add(this.mapaGlobalAtributos.get("rolekey"));
            XExtension extension = xExtensionManager.getByPrefix("org");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("group") != null) {
            this.log.getGlobalEventAttributes().add(this.mapaGlobalAtributos.get("groupkey"));
            XExtension extension = xExtensionManager.getByPrefix("org");
            this.log.getExtensions().add(extension);
        }
    }

    /**
     * This method is called by PDI once the step is done processing.
     * <p>
     * The dispose() method is the counterpart to init() and should release any resources
     * acquired for step execution like file handles or database connections.
     * <p>
     * The meta and data implementations passed in can safely be cast
     * to the step's respective implementations.
     * <p>
     * It is mandatory that super.dispose() is called to ensure correct behavior.
     *
     * @param smi step meta interface implementation, containing the step settings
     * @param sdi step data interface implementation, used to store runtime information
     */
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

        // Casting to step-specific implementation classes is safe
        DemoStepMeta meta = (DemoStepMeta) smi;
        DemoStepData data = (DemoStepData) sdi;

        super.dispose(meta, data);
    }

}

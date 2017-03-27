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

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.*;
import org.deckfour.xes.model.buffered.XAttributeMapSerializerImpl;
import org.deckfour.xes.model.buffered.XTraceBufferedImpl;
import org.deckfour.xes.model.impl.*;
import org.deckfour.xes.out.XesXmlSerializer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class XESPluginStep extends BaseStep implements StepInterface {

    private static Class<?> PKG = XESPluginStepMeta.class; // for i18n purposes

    //global attributes
    private XLogImpl log;
    private XExtensionManager xExtensionManager;
    private Map<String, String> mapa_columnas;
    private LinkedList<XTraceBufferedImpl> lista_traces;
    private Map<String, String> mapa_IPRegistradas;
    private XAttributeMapImpl mapaGlobalAtributos;
    private Map<String, Boolean> mapaUsoAtributos;
    private Map<String, XID> mapaID;
    private Map<XTraceBufferedImpl, Float> mapa_CostTotalTrace;
    private Map<XTraceBufferedImpl, String> mapa_ConceptNameTrace;
    private int rowCount;

    /**
     * The constructor should simply pass on its arguments to the parent class.
     *
     * @param s                 step description
     * @param stepDataInterface step data class
     * @param c                 step copy
     * @param t                 transformation description
     * @param dis               transformation executing
     */
    public XESPluginStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
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
        XESPluginStepMeta meta = (XESPluginStepMeta) smi;
        XESPluginStepData data = (XESPluginStepData) sdi;

        //my initializations
        XAttributeMapImpl atb_map = new XAttributeMapImpl();
        this.log = new XLogImpl(atb_map);
        this.xExtensionManager = XExtensionManager.instance();
        this.lista_traces = new LinkedList<>();
        this.mapa_columnas = new HashMap<>();
        this.mapa_IPRegistradas = new HashMap<>();
        this.mapaGlobalAtributos = new XAttributeMapImpl();
        this.mapaUsoAtributos = new HashMap<>();
        this.mapaID = new HashMap<>();
        this.mapa_CostTotalTrace = new HashMap<>();
        this.mapa_ConceptNameTrace = new HashMap<>();
        this.rowCount = 0;

        IniciarMapaGlobal();

        return super.init(meta, data);
    }

    private void IniciarMapaGlobal() {
//        this.mapaGlobalAtributos.put("conceptkey", new XAttributeLiteralImpl("concept:name", ""));
        this.mapaGlobalAtributos.put("timestampkey", new XAttributeTimestampImpl("time:timestamp", new java.util.Date("01/01/1970 00:00:00")));
        this.mapaGlobalAtributos.put("lifecyclekey", new XAttributeLiteralImpl("lifecycle:transition", "complete"));
//        this.mapaGlobalAtributos.put("resourcekey", new XAttributeLiteralImpl("org:resource", ""));
//        this.mapaGlobalAtributos.put("rolekey", new XAttributeLiteralImpl("org:role", ""));
//        this.mapaGlobalAtributos.put("groupkey", new XAttributeLiteralImpl("org:group", ""));
        this.mapaGlobalAtributos.put("levelkey", new XAttributeDiscreteImpl("micro:level", 1));
        this.mapaGlobalAtributos.put("currencykey", new XAttributeLiteralImpl("cost:currency", "USD"));
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

        //incrementando para apuntar a la fila en la que se esta trabajando
        this.rowCount++;

        // safely cast the step settings (meta) and runtime info (data) to specific implementations
        XESPluginStepMeta meta = (XESPluginStepMeta) smi;
        XESPluginStepData data = (XESPluginStepData) sdi;

        // get incoming row, getRow() potentially blocks waiting for more rows, returns null if no more rows expected
        Object[] r = getRow();

        // if no more rows are expected, indicate step is finished and processRow() should not be called again
        if (r == null) {

            setOutputDone();

            //revisando que atributos se han usado, para globales y extensiones
            UsoAtributos();

            //asignar a cada trace su costo total
            for (XTraceBufferedImpl xtrace : this.lista_traces) {
                XAttributeMapImpl xmap = new XAttributeMapImpl();
                //para no obligar a q cada trace tenga un costo, porq la extension puede no usarse
                if (this.mapaUsoAtributos.get("totaltrace") != null) {
                    xmap.put("totaltracekey", new XAttributeContinuousImpl("cost:total", this.mapa_CostTotalTrace.get(xtrace)));
                }
                xmap.put("conceptnametrace", new XAttributeLiteralImpl("concept:name", this.mapa_ConceptNameTrace.get(xtrace)));
                xtrace.setAttributes(xmap);
            }

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
        String nombresColumnas[] = data.outputRowMeta.getFieldNames();

        //Evento
        Map<String, XAttribute> map = new XAttributeMapImpl();
        XEventImpl event = new XEventImpl(new XAttributeMapImpl());

        //Inicializando variables que podrian venir desde la vista
        String valorIP = "";
        String valor_marcatiempo = "";
        String valor_actividad = "";
        String valor_cicloVida = "";
        String valor_recurso = "";
        String valor_rol = "";
        String valor_grupo = "";
        String valor_level = "";
        String valor_parentID = "";
        String valor_ID = "";
        String valor_Moneda = "";
        String valor_TotalTrace = "";
        String valor_TotalEvent = "";

        //Asignandole a mapa_columnas el valor q viene desde la vista:
        this.mapa_columnas = meta.getMapa_vista();

        for (int i = 0; i < nombresColumnas.length; i++) {
            //en dependencia del dato q sea adicionarlo al map como el tipo de atributo q es
            if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("InstanciaProceso"))) {
                valorIP = DescifrarPalabra(r[i]);
                //no puede existir un valor de IP vacio
                if (valorIP.isEmpty() || valorIP.equals("null")) {
                    throw new KettleException(BaseMessages.getString(PKG, "XESPlugin.Exceptions.ProcessInstanceNotFound") + " SOURCELINE: " + this.rowCount);
                }
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("MarcaTiempo"))) {
                valor_marcatiempo = DescifrarPalabra(r[i]);
                this.mapaUsoAtributos.put("timestamp", true);
                if (valor_marcatiempo.isEmpty() || valor_marcatiempo.equals("null")) {
                    //Valor por defecto en caso de que el timestamp sea nulo en algun momento
                    map.put("DateKey", new XAttributeTimestampImpl("time:timestamp", new java.util.Date("01/01/1970 00:00:00")));
                } else {
                    try {
                        //Lanza Excepcion que se queda como log en la consola de PentahoDI
                        Date date = new SimpleDateFormat(this.mapa_columnas.get("RegexMarcaTiempo")).parse(valor_marcatiempo);
                        map.put("DateKey", new XAttributeTimestampImpl("time:timestamp", date));
                    } catch (ParseException e) {
                        throw new KettleException(BaseMessages.getString(PKG, "XESPlugin.Exceptions.DateFormat") + " SOURCELINE: " + this.rowCount);
                    }
                }
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("Actividad"))) {
                valor_actividad = DescifrarPalabra(r[i]);
                if (!valor_actividad.isEmpty() && !valor_actividad.equals("null")) {
                    map.put("ActividadKey", new XAttributeLiteralImpl("concept:name", valor_actividad));
                    this.mapaUsoAtributos.put("concept", true);
                } else {
                    throw new KettleException(BaseMessages.getString(PKG, "XESPlugin.Exceptions.ActivityNotFound") + " SOURCELINE: " + this.rowCount);
                }
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("CicloVida"))) {
                valor_cicloVida = DescifrarPalabra(r[i]);
                if (!valor_cicloVida.isEmpty() && !valor_cicloVida.equals("null")) {
                    map.put("CicloVidaKey", new XAttributeLiteralImpl("lifecycle:transition", valor_cicloVida));
                    this.mapaUsoAtributos.put("lifecycle", true);
                }
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("Recurso"))) {
                valor_recurso = DescifrarPalabra(r[i]);
                if (!valor_recurso.isEmpty() && !valor_recurso.equals("null")) {
                    map.put("RecursoKey", new XAttributeLiteralImpl("org:resource", valor_recurso));
                    this.mapaUsoAtributos.put("resource", true);
                }
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("Rol"))) {
                valor_rol = DescifrarPalabra(r[i]);
                if (!valor_rol.isEmpty() && !valor_rol.equals("null")) {
                    map.put("RolKey", new XAttributeLiteralImpl("org:role", valor_rol));
                    this.mapaUsoAtributos.put("role", true);
                }
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("Grupo"))) {
                valor_grupo = DescifrarPalabra(r[i]);
                if (!valor_grupo.isEmpty() && !valor_grupo.equals("null")) {
                    map.put("GrupoKey", new XAttributeLiteralImpl("org:group", valor_grupo));
                    this.mapaUsoAtributos.put("group", true);
                }
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("Nivel"))) {
                valor_level = DescifrarPalabra(r[i]);
                if (!valor_level.isEmpty() && !valor_level.equals("null")) {
                    map.put("LevelKey", new XAttributeDiscreteImpl("micro:level", Integer.parseInt(valor_level)));
                    this.mapaUsoAtributos.put("level", true);
                } else {
                    map.put("LevelKey", new XAttributeDiscreteImpl("micro:level", 1));
                    this.mapaUsoAtributos.put("level", true);
                }
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("IDPadre"))) {
                valor_parentID = DescifrarPalabra(r[i]);
                if (!valor_parentID.isEmpty() && !valor_parentID.equals("null")) {
                    //TODO CODIGO Revisar como debe salir el XID, si como <id> y debe ser un <int>
                    XID xid_parent = this.mapaID.get(valor_parentID);
                    //si el valor de xid_parent es nulo, quiere decir que su padre aun no esta registrado en el sistema.
                    //esto provoca un fallo pues no se le puede asignar el id del padre correctamente
                    //para esto debe de venir el registro de eventos organizado, de manera tal que
                    //un padre siempre se registre antes que sus hijos.
                    if (xid_parent == null) {
                        throw new KettleException(BaseMessages.getString(PKG, "XESPlugin.Exceptions.ParentIDNotFound") + " SOURCELINE: " + this.rowCount);
                    }

                    map.put("ParentIDKey", new XAttributeIDImpl("micro:parentId", xid_parent));
                    this.mapaUsoAtributos.put("parentId", true);
                }
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("ID"))) {
                valor_ID = DescifrarPalabra(r[i]);
                if (!valor_ID.isEmpty() && !valor_ID.equals("null")) {
                    XID xid = event.getID();
                    this.mapaID.put(valor_ID, xid);
                    map.put("IDKey", new XAttributeIDImpl("identity:id", xid));
                    map.put("IDKeyXID", new XAttributeLiteralImpl("id", valor_ID));
                    this.mapaUsoAtributos.put("id", true);
                }
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("Moneda"))) {
                valor_Moneda = DescifrarPalabra(r[i]);
                if (!valor_Moneda.isEmpty() && !valor_Moneda.equals("null")) {
                    map.put("CurrencyKey", new XAttributeLiteralImpl("cost:currency", valor_Moneda));
                    this.mapaUsoAtributos.put("currency", true);
                }
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("EventoTotal"))) {
                valor_TotalEvent = DescifrarPalabra(r[i]);
                if (!valor_TotalEvent.isEmpty() && !valor_TotalEvent.equals("null")) {
                    map.put("TotalEventKey", new XAttributeContinuousImpl("cost:total", Float.parseFloat(valor_TotalEvent)));
                    this.mapaUsoAtributos.put("totalevent", true);
                }
            } else if (nombresColumnas[i].equalsIgnoreCase(this.mapa_columnas.get("TraceTotal"))) {
                valor_TotalTrace = DescifrarPalabra(r[i]);
                if (!valor_TotalTrace.isEmpty() && !valor_TotalTrace.equals("null")) {
                    this.mapaUsoAtributos.put("totaltrace", true);
                }
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


            //adicionando la extension por instancia de procesos utilizada al log.
            XExtension extension = xExtensionManager.getByPrefix("concept");
            this.log.getExtensions().add(extension);

            XTraceBufferedImpl trace = new XTraceBufferedImpl(new XAttributeMapImpl(), new XAttributeMapSerializerImpl());
            trace.add(event);
            this.lista_traces.add(trace);

            //actualizando mapaIPRegistradas
            this.mapa_IPRegistradas.put(valorIP, String.valueOf(mapa_IPRegistradas.size())); //=mente pudiera ser size de lista_traces
            //adicionando el concept name al trace
            this.mapa_ConceptNameTrace.put(trace, valorIP);
            if (!valor_TotalTrace.isEmpty()) {
                //actualizando mapa con costo total de trace
                this.mapa_CostTotalTrace.put(trace, Float.parseFloat(valor_TotalTrace));
            }
        } else {
            //Pseudo-codigo:: ;)
            //"pos" tiene la posicion de lista de traces en la q se trata a esa IP (pos es "String")
            // se vincula el event con el trace q esta en esa posicion devuelta

            this.lista_traces.get(Integer.parseInt(pos_lista_traces)).add(event);

            if (!valor_TotalTrace.isEmpty()) {
                //actualizando mapa con costo total de trace
                this.mapa_CostTotalTrace.put(this.lista_traces.get(Integer.parseInt(pos_lista_traces)), Float.parseFloat(valor_TotalTrace));
            }
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
    private static boolean arrayContains(String[] array, String valor) {
        for (String s : array) {
            if (s.equalsIgnoreCase(valor)) {
                return true;
            }
        }
        return false;
    }

    private String DescifrarPalabra(Object arr) {
        if (arr instanceof byte[]) { //works !
            byte[] input = ((byte[]) arr);
            return new String(input);
        }
        return String.valueOf(arr);
    }

    private void UsoAtributos() {
        if (this.mapaUsoAtributos.get("concept") != null) {
//            this.log.getGlobalEventAttributes().add(this.mapaGlobalAtributos.get("conceptkey"));
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
//            this.log.getGlobalEventAttributes().add(this.mapaGlobalAtributos.get("resourcekey"));
            XExtension extension = xExtensionManager.getByPrefix("org");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("role") != null) {
//            this.log.getGlobalEventAttributes().add(this.mapaGlobalAtributos.get("rolekey"));
            XExtension extension = xExtensionManager.getByPrefix("org");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("group") != null) {
//            this.log.getGlobalEventAttributes().add(this.mapaGlobalAtributos.get("groupkey"));
            XExtension extension = xExtensionManager.getByPrefix("org");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("level") != null) {
            this.log.getGlobalEventAttributes().add(this.mapaGlobalAtributos.get("levelkey"));
            XExtension extension = xExtensionManager.getByPrefix("micro");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("parentId") != null) {
            XExtension extension = xExtensionManager.getByPrefix("micro");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("id") != null) {
            XExtension extension = xExtensionManager.getByPrefix("identity");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("currency") != null) {
            this.log.getGlobalEventAttributes().add(this.mapaGlobalAtributos.get("currencykey"));
            XExtension extension = xExtensionManager.getByPrefix("cost");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("totalevent") != null) {
            XExtension extension = xExtensionManager.getByPrefix("cost");
            this.log.getExtensions().add(extension);
        }
        if (this.mapaUsoAtributos.get("totaltrace") != null) {
            XExtension extension = xExtensionManager.getByPrefix("cost");
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
        XESPluginStepMeta meta = (XESPluginStepMeta) smi;
        XESPluginStepData data = (XESPluginStepData) sdi;

        super.dispose(meta, data);
    }

}

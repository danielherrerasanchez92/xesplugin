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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleAuthException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.ShowMessageDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.core.Props;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XESPluginStepDialog extends BaseStepDialog implements StepDialogInterface {

    /**
     * The PKG member is used when looking up internationalized strings.
     * The properties file with localized keys is expected to reside in
     * {the package of the class specified}/messages/messages_{locale}.properties
     */

    // this is the object the stores the step's settings
    // the dialog reads the settings from it when opening
    // the dialog writes the settings to it when confirmed

    private static Class<?> PKG = XESPluginStepMeta.class; // for i18n purposes

    private XESPluginStepMeta meta;
    // Outputpath
    private TextVar wRutaSalida;
    private Listener lsRuta;

    private CTabFolder wTabFolder;

    /*
    *  Identity extension
    * */
    private CTabItem wIDTab;
    private Composite wIDComp;
    private Group wIDGroup;
    private Combo cmbID;

    /*
    * Lifecycle and concept extensions
    * */
    private CTabItem wProcessTab;
    private Composite wProcessComp;
    private Group wProcessGroup;

    private Combo cmbIProceso;
    private Combo cmbActividad;
    private Combo cmbCicloVida;
    private Combo cmbActivityInstans;

    /*
    * Time extension
    * */
    private CTabItem wTimeTab;
    private Composite wTimeComp;
    private Group wTimeGroup;

    private Combo cmbTimeStamp;
    private Label wlRegex;
    private Combo cmbregexTimeStamp;
    private TextVar wNuevaRegex1;
    private Button btnNuevaRegex;
    private Label wlNuevaRegex;
    private Listener lsNuevaRegex;
    private String regexItems[] = {
            "dd.MM.yyyy hh:mm:ss",
            "dd/MM/yyyy hh:mm:ss",
            "dd-MM-yyyy hh:mm:ss",
            "yyyy.MM.dd hh:mm:ss",
            "yyyy/MM/dd hh:mm:ss",
            "yyyy-MM-dd hh:mm:ss"
    };

    /*
    * ORG extension
    * */
    private CTabItem wResourceTab;
    private Composite wResourceComp;
    private Group wResourceGroup;

    private Combo cmbRecurso;
    private Combo cmbRol;
    private Combo cmbGrupo;

    /*
    * Micro extension
    * */
    private CTabItem wMicroTab;
    private Composite wMicroComp;
    private Group wMicroGroup;

    private Combo cmbLevel;
    private Combo cmbParentID;

    /*
    * Cost extension
    * */
    private CTabItem wCostTab;
    private Composite wCostComp;
    private Group wCostGroup;

    private Combo cmbCostTotalTrace;
    private Combo cmbCostTotalEvent;
    private Combo cmbCostCurrency;

    /*
    * Generales
    * */
    private String[] nombresDeColumnas;
    private int middle;
    private int margin;
    private ModifyListener lsMod;

    //** Table of new atributes
    private CTabItem wFieldsTab;
    private Composite wFieldsComp;


    private TableView wFields;
    private FormData fdFields;
    private String [] Tipos={"","Case","Activity"};
    private String [] Datos={"","String","Integer", "Date", "Float", "Boolean", "ID"};
    private int cont=0;

    private ColumnInfo[] colinf;
    private Map<Integer,XESPluginField> newatrib = new HashMap<>();
    int numFields;

    /**
     * The constructor should simply invoke super() and save the incoming meta
     * object to a local variable, so it can conveniently read and write settings
     * from/to it.
     *
     * @param parent    the SWT shell to open the dialog in
     * @param in        the meta object holding the step's settings
     * @param transMeta transformation description
     * @param sname     the step name
     */
    public XESPluginStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
        super(parent, (BaseStepMeta) in, transMeta, sname);
        meta = (XESPluginStepMeta) in;
    }

    /**
     * This method is called by Spoon when the user opens the settings dialog of the step.
     * It should open the dialog and return only once the dialog has been closed by the user.
     * <p>
     * If the user confirms the dialog, the meta object (passed in the constructor) must
     * be updated to reflect the new step settings. The changed flag of the meta object must
     * reflect whether the step configuration was changed by the dialog.
     * <p>
     * If the user cancels the dialog, the meta object must not be updated, and its changed flag
     * must remain unaltered.
     * <p>
     * The open() method must return the name of the step after the user has confirmed the dialog,
     * or null if the user cancelled the dialog.
     */
    public String open() {

        // store some convenient SWT variables
        Shell parent = getParent();
        Display display = parent.getDisplay();

        // SWT code for preparing the dialog
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, meta);

        // Save the value of the changed flag on the meta object. If the user cancels
        // the dialog, it will be restored to this saved value.
        // The "changed" variable is inherited from BaseStepDialog
        changed = meta.hasChanged();

        // The ModifyListener used on all controls. It will update the meta object to
        // indicate that changes are being made.
        this.lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                meta.setChanged();
            }
        };

        // ------------------------------------------------------- //
        // SWT code for building the actual settings dialog        //
        // ------------------------------------------------------- //
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "XESPlugin.Shell.Title"));

        this.middle = props.getMiddlePct();
        this.margin = Const.MARGIN;

        //los nombres de las columnas de pasos anteriores
        try {
            RowMetaInterface inputFields = transMeta.getPrevStepFields(stepMeta);
            if (inputFields != null) {
                this.nombresDeColumnas = inputFields.getFieldNames();
            }
        } catch (Exception e) {
            new ErrorDialog(shell, "Error", "Error obtaining list of input fields:", e);
        }

        // nombre del paso
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "XESPlugin.Shell.Stepname"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        wlStepname.setLayoutData(fdlStepname);
        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, margin);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        //para ruta de salida
        Label wlRutaSalida = new Label(shell, SWT.RIGHT);
        wlRutaSalida.setText(BaseMessages.getString(PKG, "XESPlugin.Shell.Outputpath"));
        props.setLook(wlRutaSalida);
        FormData fdlRutaSalida = new FormData();
        fdlRutaSalida.left = new FormAttachment(0, 0);
        fdlRutaSalida.top = new FormAttachment(wStepname, margin);
        fdlRutaSalida.right = new FormAttachment(middle, -margin);
        wlRutaSalida.setLayoutData(fdlRutaSalida);

        Button btnRuta = new Button(shell, SWT.PUSH | SWT.CENTER);
        btnRuta.setText(BaseMessages.getString(PKG, "XESPlugin.Shell.BrowseButton"));
        props.setLook(btnRuta);
        FormData fdBtnRuta = new FormData();
        fdBtnRuta.right = new FormAttachment(100, 0);
        fdBtnRuta.top = new FormAttachment(wStepname, margin);
        btnRuta.setLayoutData(fdBtnRuta);

        wRutaSalida = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wRutaSalida);
        wRutaSalida.addModifyListener(lsMod);
        FormData fdRutaSalida = new FormData();
        fdRutaSalida.left = new FormAttachment(middle, margin);
        fdRutaSalida.top = new FormAttachment(wStepname, margin);
        fdRutaSalida.right = new FormAttachment(btnRuta, -margin);
        wRutaSalida.setLayoutData(fdRutaSalida);

        //TabFolder
        wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

        //Llama a los tabs para crearlos en orden especifico
        AddProcessTab();
        AddTimeTab();
        AddResourceTab();
        AddIdentityTab();
        AddMicroTab();
        AddCostTab();
        field();

        //Setting tooltip text
        SetTooltipText();

        //Datos de TABS
        FormData fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment(0, 0);
        fdTabFolder.top = new FormAttachment(btnRuta, margin);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.bottom = new FormAttachment(100, -50);
        wTabFolder.setLayoutData(fdTabFolder);
        wTabFolder.setSelection(0);

        // OK and cancel buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "XESPlugin.Shell.ButtonOK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "XESPlugin.Shell.ButtonCancel"));

        BaseStepDialog.positionBottomRightButtons(shell, new Button[]{wOK, wCancel}, margin, wTabFolder);

        //***********************
        // Seccion de Listeners *
        //***********************

        // Add listeners for cancel and OK
        lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };
        lsOK = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        //listener para boton de la ruta
        lsRuta = new Listener() {
            public void handleEvent(Event e) {
                OutputPathShow();
            }
        };
        btnRuta.addListener(SWT.Selection, lsRuta);

        // default listener (for hitting "enter")
        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };
        wStepname.addSelectionListener(lsDef);
        cmbIProceso.addSelectionListener(lsDef);
        cmbActividad.addSelectionListener(lsDef);
        cmbActivityInstans.addSelectionListener(lsDef);
        cmbTimeStamp.addSelectionListener(lsDef);
        cmbregexTimeStamp.addSelectionListener(lsDef);
        cmbCicloVida.addSelectionListener(lsDef);
        cmbRecurso.addSelectionListener(lsDef);
        cmbRol.addSelectionListener(lsDef);
        cmbGrupo.addSelectionListener(lsDef);
        cmbLevel.addSelectionListener(lsDef);
        cmbParentID.addSelectionListener(lsDef);
        cmbID.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        // Set/Restore the dialog size based on last position on screen
        // The setSize() method is inherited from BaseStepDialog
        setSize();
        // populate the dialog with the values from the meta object
        populateDialog();

        // restore the changed flag to original value, as the modify listeners fire during dialog population
        meta.setChanged(changed);

        // open dialog and enter event loop
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        // at this point the dialog has closed, so either ok() or cancel() have been executed
        // The "stepname" variable is inherited from BaseStepDialog
        return stepname;
    }
    // tooltip Text of comboboxes
    private void SetTooltipText(){
        cmbID.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.ID"));
        cmbIProceso.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.IP"));
        cmbActividad.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Activity"));
        cmbCicloVida.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Lifecycle"));
        cmbActivityInstans.setToolTipText(BaseMessages.getString(PKG, "XESPluginTooltip.Activity_Instans"));
        cmbTimeStamp.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Timestamp"));
        cmbregexTimeStamp.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Timestamp_regex_select"));
        wNuevaRegex1.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Timestamp_regex_write"));
        cmbRecurso.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Resource"));
        cmbRol.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Role"));
        cmbGrupo.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Group"));
        cmbLevel.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Level"));
        cmbParentID.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.ParentID"));
        cmbCostTotalTrace.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.TotalTrace"));
        cmbCostTotalEvent.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.TotalEvent"));
        cmbCostCurrency.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Currency"));
        cmbActivityInstans.setToolTipText(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Activity_Instans"));


    }

    private void AddProcessTab() {
        //*************
        //Process tab *
        //*************
        wProcessTab = new CTabItem(wTabFolder, SWT.NONE);
        wProcessTab.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Process.Tabname"));

        wProcessComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wProcessComp);

        FormLayout processLayout = new FormLayout();
        processLayout.marginWidth = 3;
        processLayout.marginHeight = 3;
        wProcessComp.setLayout(processLayout);

        wProcessGroup = new Group(wProcessComp, SWT.SHADOW_NONE);
        props.setLook(wProcessGroup);
        wProcessGroup.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Process.Gropuname"));

        FormLayout processgroupLayout = new FormLayout();
        processgroupLayout.marginWidth = 10;
        processgroupLayout.marginHeight = 10;
        wProcessGroup.setLayout(processgroupLayout);

        // para instancia de proceso
        Label wlIP = new Label(wProcessGroup, SWT.RIGHT);
        wlIP.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Process.Case"));
        props.setLook(wlIP);
        FormData fdlIP = new FormData();
        fdlIP.left = new FormAttachment(0, 0);
        fdlIP.right = new FormAttachment(middle, -margin);
        fdlIP.top = new FormAttachment(0, margin);
        wlIP.setLayoutData(fdlIP);

        cmbIProceso = new Combo(wProcessGroup, SWT.READ_ONLY);
        props.setLook(cmbIProceso);
        cmbIProceso.addModifyListener(lsMod);
        cmbIProceso.setItems(nombresDeColumnas);
        FormData fdIP = new FormData();
        fdIP.left = new FormAttachment(middle, 0);
        fdIP.right = new FormAttachment(100, 0);
        fdIP.top = new FormAttachment(0, margin);
        cmbIProceso.setLayoutData(fdIP);

        //para actividad
        Label wlActividad = new Label(wProcessGroup, SWT.RIGHT);
        wlActividad.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Process.Activity"));
        props.setLook(wlActividad);
        FormData fdlActividad = new FormData();
        fdlActividad.left = new FormAttachment(0, 0);
        fdlActividad.right = new FormAttachment(middle, -margin);
        fdlActividad.top = new FormAttachment(cmbIProceso, margin);
        wlActividad.setLayoutData(fdlActividad);

        cmbActividad = new Combo(wProcessGroup, SWT.READ_ONLY);
        props.setLook(cmbActividad);
        cmbActividad.addModifyListener(lsMod);
        cmbActividad.setItems(nombresDeColumnas);
        FormData fdActividad = new FormData();
        fdActividad.left = new FormAttachment(middle, 0);
        fdActividad.right = new FormAttachment(100, 0);
        fdActividad.top = new FormAttachment(cmbIProceso, margin);
        cmbActividad.setLayoutData(fdActividad);

        //para ciclo de vida
        Label wlCicloVida = new Label(wProcessGroup, SWT.RIGHT);
        wlCicloVida.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Process.Lifecycle"));
        props.setLook(wlCicloVida);
        FormData fdlCicloVida = new FormData();
        fdlCicloVida.left = new FormAttachment(0, 0);
        fdlCicloVida.right = new FormAttachment(middle, -margin);
        fdlCicloVida.top = new FormAttachment(cmbActividad, margin);
        wlCicloVida.setLayoutData(fdlCicloVida);

        cmbCicloVida = new Combo(wProcessGroup, SWT.READ_ONLY);
        props.setLook(cmbCicloVida);
        cmbCicloVida.addModifyListener(lsMod);
        cmbCicloVida.setItems(nombresDeColumnas);
        FormData fdCicloVida = new FormData();
        fdCicloVida.left = new FormAttachment(middle, 0);
        fdCicloVida.right = new FormAttachment(100, 0);
        fdCicloVida.top = new FormAttachment(cmbActividad, margin);
        cmbCicloVida.setLayoutData(fdCicloVida);

        //para activity instans
        Label wlact = new Label(wProcessGroup, SWT.RIGHT);
        wlact.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Process.Activity_Instans"));
        props.setLook(wlact);
        FormData fdlactv = new FormData();
        fdlactv.left = new FormAttachment(0, 0);
        fdlactv.right = new FormAttachment(middle, -margin);
        fdlactv.top = new FormAttachment(cmbCicloVida, margin);
        wlact.setLayoutData(fdlactv);

        cmbActivityInstans = new Combo(wProcessGroup, SWT.READ_ONLY);
        props.setLook(cmbActivityInstans);
        cmbActivityInstans.addModifyListener(lsMod);
        cmbActivityInstans.setItems(nombresDeColumnas);
        FormData fdact = new FormData();
        fdact.left = new FormAttachment(middle, 0);
        fdact.right = new FormAttachment(100, 0);
        fdact.top = new FormAttachment(cmbCicloVida, margin);
        cmbActivityInstans.setLayoutData(fdact);

        FormData fdProcessGroup = new FormData();
        fdProcessGroup.left = new FormAttachment(0, margin);
        fdProcessGroup.top = new FormAttachment(0, margin);
        fdProcessGroup.right = new FormAttachment(100, -margin);
        wProcessGroup.setLayoutData(fdProcessGroup);

        FormData fdProcessComp = new FormData();
        fdProcessComp.left = new FormAttachment(0, 0);
        fdProcessComp.top = new FormAttachment(0, 0);
        fdProcessComp.right = new FormAttachment(100, 0);
        fdProcessComp.bottom = new FormAttachment(100, 0);
        wProcessComp.setLayoutData(fdProcessComp);

        wProcessComp.layout();
        wProcessTab.setControl(wProcessComp);
        props.setLook(wProcessComp);
    }

    private void AddTimeTab() {
        //************************
        //TimeStamp + Regex tabs *
        //************************
        wTimeTab = new CTabItem(wTabFolder, SWT.NONE);
        wTimeTab.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Timestamp.Tabname"));

        wTimeComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wTimeComp);

        FormLayout timeLayout = new FormLayout();
        timeLayout.marginWidth = 3;
        timeLayout.marginHeight = 3;
        wTimeComp.setLayout(timeLayout);

        wTimeGroup = new Group(wTimeComp, SWT.SHADOW_NONE);
        props.setLook(wTimeGroup);
        wTimeGroup.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Timestamp.Groupname"));
        FormLayout timegroupLayout = new FormLayout();
        timegroupLayout.marginWidth = 10;
        timegroupLayout.marginHeight = 10;
        wTimeGroup.setLayout(timegroupLayout);

        //para marca de tiempo
        Label wlTimeStamp = new Label(wTimeGroup, SWT.RIGHT);
        wlTimeStamp.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Timestamp.Timestamp"));
        props.setLook(wlTimeStamp);
        FormData fdlTimeStamp = new FormData();
        fdlTimeStamp.left = new FormAttachment(0, 0);
        fdlTimeStamp.right = new FormAttachment(middle, -margin);
        fdlTimeStamp.top = new FormAttachment(0, margin);
        wlTimeStamp.setLayoutData(fdlTimeStamp);

        cmbTimeStamp = new Combo(wTimeGroup, SWT.READ_ONLY);
        props.setLook(cmbTimeStamp);
        cmbTimeStamp.addModifyListener(lsMod);
        cmbTimeStamp.setItems(nombresDeColumnas);
        FormData fdTimeStamp = new FormData();
        fdTimeStamp.left = new FormAttachment(middle, 0);
        fdTimeStamp.right = new FormAttachment(100, 0);
        fdTimeStamp.top = new FormAttachment(0, margin);
        cmbTimeStamp.setLayoutData(fdTimeStamp);

        // para expresion regular de timestamp
        wlRegex = new Label(wTimeGroup, SWT.RIGHT);
        wlRegex.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Timestamp.Format"));
        props.setLook(wlRegex);
        FormData fdlRegex = new FormData();
        fdlRegex.left = new FormAttachment(0, 0);
        fdlRegex.right = new FormAttachment(middle, -margin);
        fdlRegex.top = new FormAttachment(cmbTimeStamp, margin);
        wlRegex.setLayoutData(fdlRegex);

        cmbregexTimeStamp = new Combo(wTimeGroup, SWT.READ_ONLY);
        cmbregexTimeStamp.setItems(regexItems);
        props.setLook(cmbregexTimeStamp);
        cmbregexTimeStamp.addModifyListener(lsMod);
        FormData fdRegex = new FormData();
        fdRegex.left = new FormAttachment(middle, 0);
        fdRegex.right = new FormAttachment(100, 0);
        fdRegex.top = new FormAttachment(cmbTimeStamp, margin);
        cmbregexTimeStamp.setLayoutData(fdRegex);

        //Para especificar una nueva expresion regular para la fecha
        btnNuevaRegex = new Button(wTimeGroup, SWT.CHECK);
        btnNuevaRegex.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Timestamp.Chknewformat"));
        props.setLook(btnNuevaRegex);
        FormData fdcNuevaRegex = new FormData();
        fdcNuevaRegex.left = new FormAttachment(middle, 0);
        fdcNuevaRegex.right = new FormAttachment(100, 0);
        fdcNuevaRegex.top = new FormAttachment(cmbregexTimeStamp, margin);
        btnNuevaRegex.setLayoutData(fdcNuevaRegex);

        wlNuevaRegex = new Label(wTimeGroup, SWT.RIGHT);
        wlNuevaRegex.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Timestamp.Newformat"));
        props.setLook(wlNuevaRegex);
        FormData fdlNuevaRegex = new FormData();
        fdlNuevaRegex.left = new FormAttachment(0, 0);
        fdlNuevaRegex.right = new FormAttachment(middle, -margin);
        fdlNuevaRegex.top = new FormAttachment(btnNuevaRegex, margin);
        wlNuevaRegex.setLayoutData(fdlNuevaRegex);
        wlNuevaRegex.setEnabled(false);

       // wNuevaRegex1 = new Text(wTimeGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wNuevaRegex1 = new TextVar(transMeta, wTimeGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wNuevaRegex1);
        wNuevaRegex1.addModifyListener(lsMod);
        FormData fdNuevaRegex = new FormData();
        fdNuevaRegex.left = new FormAttachment(middle, 0);
        fdNuevaRegex.top = new FormAttachment(btnNuevaRegex, margin);
        fdNuevaRegex.right = new FormAttachment(100, 0);
        wNuevaRegex1.setLayoutData(fdNuevaRegex);
        wNuevaRegex1.setEnabled(false);
        //
        //listener para adicionar una nueva expresion regular
        lsNuevaRegex = new Listener() {
            public void handleEvent(Event e) {
                //codigo para nuevo regex
                    boolean status = btnNuevaRegex.getSelection();
                    wNuevaRegex1.setEnabled(status);
                    wlNuevaRegex.setEnabled(status);
                    cmbregexTimeStamp.setEnabled(!status);
                    wlRegex.setEnabled(!status);
            }
        };
        btnNuevaRegex.addListener(SWT.Selection, lsNuevaRegex);

        FormData fdTimeGroup = new FormData();
        fdTimeGroup.left = new FormAttachment(0, margin);
        fdTimeGroup.top = new FormAttachment(0, margin);
        fdTimeGroup.right = new FormAttachment(100, -margin);
        wTimeGroup.setLayoutData(fdTimeGroup);

        FormData fdTimeComp = new FormData();
        fdTimeComp.left = new FormAttachment(0, 0);
        fdTimeComp.top = new FormAttachment(0, 0);
        fdTimeComp.right = new FormAttachment(100, 0);
        fdTimeComp.bottom = new FormAttachment(100, 0);
        wTimeComp.setLayoutData(fdTimeComp);

        wTimeComp.layout();
        wTimeTab.setControl(wTimeComp);
        props.setLook(wTimeComp);
    }

    private void AddResourceTab() {
        //***************
        //Resources tab *
        //***************
        wResourceTab = new CTabItem(wTabFolder, SWT.NONE);
        wResourceTab.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Resource.Tabname"));

        wResourceComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wResourceComp);

        FormLayout resourceLayout = new FormLayout();
        resourceLayout.marginWidth = 3;
        resourceLayout.marginHeight = 3;
        wResourceComp.setLayout(resourceLayout);

        wResourceGroup = new Group(wResourceComp, SWT.SHADOW_NONE);
        props.setLook(wResourceGroup);
        wResourceGroup.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Resource.Groupname"));
        FormLayout resourcesgroupLayout = new FormLayout();
        resourcesgroupLayout.marginWidth = 10;
        resourcesgroupLayout.marginHeight = 10;
        wResourceGroup.setLayout(resourcesgroupLayout);

        //para Recurso
        Label wlRecurso = new Label(wResourceGroup, SWT.RIGHT);
        wlRecurso.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Resource.Resource"));
        props.setLook(wlRecurso);
        FormData fdlRecurso = new FormData();
        fdlRecurso.left = new FormAttachment(0, 0);
        fdlRecurso.right = new FormAttachment(middle, -margin);
        fdlRecurso.top = new FormAttachment(0, margin);
        wlRecurso.setLayoutData(fdlRecurso);

        cmbRecurso = new Combo(wResourceGroup, SWT.READ_ONLY);
        props.setLook(cmbRecurso);
        cmbRecurso.addModifyListener(lsMod);
        cmbRecurso.setItems(nombresDeColumnas);
        FormData fdRecurso = new FormData();
        fdRecurso.left = new FormAttachment(middle, 0);
        fdRecurso.right = new FormAttachment(100, 0);
        fdRecurso.top = new FormAttachment(0, margin);
        cmbRecurso.setLayoutData(fdRecurso);

        //para Rol
        Label wlRol = new Label(wResourceGroup, SWT.RIGHT);
        wlRol.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Resource.Role"));
        props.setLook(wlRol);
        FormData fdlRol = new FormData();
        fdlRol.left = new FormAttachment(0, 0);
        fdlRol.right = new FormAttachment(middle, -margin);
        fdlRol.top = new FormAttachment(cmbRecurso, margin);
        wlRol.setLayoutData(fdlRol);

        cmbRol = new Combo(wResourceGroup, SWT.READ_ONLY);
        props.setLook(cmbRol);
        cmbRol.addModifyListener(lsMod);
        cmbRol.setItems(nombresDeColumnas);
        FormData fdRol = new FormData();
        fdRol.left = new FormAttachment(middle, 0);
        fdRol.right = new FormAttachment(100, 0);
        fdRol.top = new FormAttachment(cmbRecurso, margin);
        cmbRol.setLayoutData(fdRol);

        //para Grupo
        Label wlGrupo = new Label(wResourceGroup, SWT.RIGHT);
        wlGrupo.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Resource.Group"));
        props.setLook(wlGrupo);
        FormData fdlGrupo = new FormData();
        fdlGrupo.left = new FormAttachment(0, 0);
        fdlGrupo.right = new FormAttachment(middle, -margin);
        fdlGrupo.top = new FormAttachment(cmbRol, margin);
        wlGrupo.setLayoutData(fdlGrupo);

        cmbGrupo = new Combo(wResourceGroup, SWT.READ_ONLY);
        props.setLook(cmbGrupo);
        cmbGrupo.addModifyListener(lsMod);
        cmbGrupo.setItems(nombresDeColumnas);
        FormData fdGrupo = new FormData();
        fdGrupo.left = new FormAttachment(middle, 0);
        fdGrupo.right = new FormAttachment(100, 0);
        fdGrupo.top = new FormAttachment(cmbRol, margin);
        cmbGrupo.setLayoutData(fdGrupo);

        FormData fdResourcesGroup = new FormData();
        fdResourcesGroup.left = new FormAttachment(0, margin);
        fdResourcesGroup.top = new FormAttachment(0, margin);
        fdResourcesGroup.right = new FormAttachment(100, -margin);
        wResourceGroup.setLayoutData(fdResourcesGroup);

        FormData fdResourcesComp = new FormData();
        fdResourcesComp.left = new FormAttachment(0, 0);
        fdResourcesComp.top = new FormAttachment(0, 0);
        fdResourcesComp.right = new FormAttachment(100, 0);
        fdResourcesComp.bottom = new FormAttachment(100, 0);
        wResourceComp.setLayoutData(fdResourcesComp);

        wResourceComp.layout();
        wResourceTab.setControl(wResourceComp);
        props.setLook(wResourceComp);
    }

    private void AddIdentityTab() {
        //**************
        // Identity tab*
        //**************

        wIDTab = new CTabItem(wTabFolder, SWT.NONE);
        wIDTab.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Identity.Tabname"));
        wIDComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wIDComp);

        FormLayout idLayout = new FormLayout();
        idLayout.marginWidth = 3;
        idLayout.marginHeight = 3;
        wIDComp.setLayout(idLayout);

        wIDGroup = new Group(wIDComp, SWT.SHADOW_NONE);
        props.setLook(wIDGroup);
        wIDGroup.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Identity.Groupname"));

        FormLayout idgroupLayout = new FormLayout();
        idgroupLayout.marginWidth = 10;
        idgroupLayout.marginHeight = 10;
        wIDGroup.setLayout(idgroupLayout);

        //para ID
        Label wlID = new Label(wIDGroup, SWT.RIGHT);
        wlID.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Identity.ID"));
        props.setLook(wlID);

        FormData fdlID = new FormData();
        fdlID.left = new FormAttachment(0, 0);
        fdlID.right = new FormAttachment(middle, -margin);
        fdlID.top = new FormAttachment(0, margin);
        wlID.setLayoutData(fdlID);

        cmbID = new Combo(wIDGroup, SWT.READ_ONLY);
        props.setLook(cmbID);
        cmbID.addModifyListener(lsMod);
        cmbID.setItems(nombresDeColumnas);
        FormData fdID = new FormData();
        fdID.left = new FormAttachment(middle, 0);
        fdID.right = new FormAttachment(100, 0);
        fdID.top = new FormAttachment(0, margin);
        cmbID.setLayoutData(fdID);

        FormData fdIDGroup = new FormData();
        fdIDGroup.left = new FormAttachment(0, margin);
        fdIDGroup.top = new FormAttachment(0, margin);
        fdIDGroup.right = new FormAttachment(100, -margin);
        wIDGroup.setLayoutData(fdIDGroup);

        FormData fdIDComp = new FormData();
        fdIDComp.left = new FormAttachment(0, 0);
        fdIDComp.top = new FormAttachment(0, 0);
        fdIDComp.right = new FormAttachment(100, 0);
        fdIDComp.bottom = new FormAttachment(100, 0);
        wIDComp.setLayoutData(fdIDComp);

        wIDComp.layout();
        wIDTab.setControl(wIDComp);
        props.setLook(wIDComp);
    }

    private void AddMicroTab() {
        //*************
        //Micro tab *
        //*************
        wMicroTab = new CTabItem(wTabFolder, SWT.NONE);
        wMicroTab.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Micro.Tabname"));

        wMicroComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wMicroComp);

        FormLayout microLayout = new FormLayout();
        microLayout.marginWidth = 3;
        microLayout.marginHeight = 3;
        wMicroComp.setLayout(microLayout);

        wMicroGroup = new Group(wMicroComp, SWT.SHADOW_NONE);
        props.setLook(wMicroGroup);
        wMicroGroup.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Micro.Groupname"));

        FormLayout microgroupLayout = new FormLayout();
        microgroupLayout.marginWidth = 10;
        microgroupLayout.marginHeight = 10;
        wMicroGroup.setLayout(microgroupLayout);

        // para nivel
        Label wlNivel = new Label(wMicroGroup, SWT.RIGHT);
        wlNivel.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Micro.Level"));
        props.setLook(wlNivel);
        FormData fdlNivel = new FormData();
        fdlNivel.left = new FormAttachment(0, 0);
        fdlNivel.right = new FormAttachment(middle, -margin);
        fdlNivel.top = new FormAttachment(0, margin);
        wlNivel.setLayoutData(fdlNivel);

        cmbLevel = new Combo(wMicroGroup, SWT.READ_ONLY);
        props.setLook(cmbLevel);
        cmbLevel.addModifyListener(lsMod);
        cmbLevel.setItems(nombresDeColumnas);
        FormData fdNivel = new FormData();
        fdNivel.left = new FormAttachment(middle, 0);
        fdNivel.right = new FormAttachment(100, 0);
        fdNivel.top = new FormAttachment(0, margin);
        cmbLevel.setLayoutData(fdNivel);

        //para parentID
        Label wlparentID = new Label(wMicroGroup, SWT.RIGHT);
        wlparentID.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Micro.ParentID"));
        props.setLook(wlparentID);
        FormData fdlParentID = new FormData();
        fdlParentID.left = new FormAttachment(0, 0);
        fdlParentID.right = new FormAttachment(middle, -margin);
        fdlParentID.top = new FormAttachment(cmbLevel, margin);
        wlparentID.setLayoutData(fdlParentID);

        cmbParentID = new Combo(wMicroGroup, SWT.READ_ONLY);
        props.setLook(cmbParentID);
        cmbParentID.addModifyListener(lsMod);
        cmbParentID.setItems(nombresDeColumnas);
        FormData fdParentID = new FormData();
        fdParentID.left = new FormAttachment(middle, 0);
        fdParentID.right = new FormAttachment(100, 0);
        fdParentID.top = new FormAttachment(cmbLevel, margin);
        cmbParentID.setLayoutData(fdParentID);

        FormData fdMicroGroup = new FormData();
        fdMicroGroup.left = new FormAttachment(0, margin);
        fdMicroGroup.top = new FormAttachment(0, margin);
        fdMicroGroup.right = new FormAttachment(100, -margin);
        wMicroGroup.setLayoutData(fdMicroGroup);

        FormData fdMicroComp = new FormData();
        fdMicroComp.left = new FormAttachment(0, 0);
        fdMicroComp.top = new FormAttachment(0, 0);
        fdMicroComp.right = new FormAttachment(100, 0);
        fdMicroComp.bottom = new FormAttachment(100, 0);
        wMicroComp.setLayoutData(fdMicroComp);

        wMicroComp.layout();
        wMicroTab.setControl(wMicroComp);
        props.setLook(wMicroComp);
    }

    private void AddCostTab() {
        //*************
        //Cost tab *
        //*************
        wCostTab = new CTabItem(wTabFolder, SWT.NONE);
        wCostTab.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Cost.Tabname"));

        wCostComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wCostComp);

        FormLayout costLayout = new FormLayout();
        costLayout.marginWidth = 3;
        costLayout.marginHeight = 3;
        wCostComp.setLayout(costLayout);

        wCostGroup = new Group(wCostComp, SWT.SHADOW_NONE);
        props.setLook(wCostGroup);
        wCostGroup.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Cost.Groupname"));

        FormLayout costgroupLayout = new FormLayout();
        costgroupLayout.marginWidth = 10;
        costgroupLayout.marginHeight = 10;
        wCostGroup.setLayout(costgroupLayout);

        // para total trace
        Label wlTotalTrace = new Label(wCostGroup, SWT.RIGHT);
        wlTotalTrace.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Cost.Tracetotal"));
        props.setLook(wlTotalTrace);
        FormData fdlTotalTrace = new FormData();
        fdlTotalTrace.left = new FormAttachment(0, 0);
        fdlTotalTrace.right = new FormAttachment(middle, -margin);
        fdlTotalTrace.top = new FormAttachment(0, margin);
        wlTotalTrace.setLayoutData(fdlTotalTrace);

        cmbCostTotalTrace = new Combo(wCostGroup, SWT.READ_ONLY);
        props.setLook(cmbCostTotalTrace);
        cmbCostTotalTrace.addModifyListener(lsMod);
        cmbCostTotalTrace.setItems(nombresDeColumnas);
        FormData fdTotalTrace = new FormData();
        fdTotalTrace.left = new FormAttachment(middle, 0);
        fdTotalTrace.right = new FormAttachment(100, 0);
        fdTotalTrace.top = new FormAttachment(0, margin);
        cmbCostTotalTrace.setLayoutData(fdTotalTrace);

        // para total event
        Label wlTotalEvent = new Label(wCostGroup, SWT.RIGHT);
        wlTotalEvent.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Cost.Eventtotal"));
        props.setLook(wlTotalEvent);
        FormData fdlTotalEvent = new FormData();
        fdlTotalEvent.left = new FormAttachment(0, 0);
        fdlTotalEvent.right = new FormAttachment(middle, -margin);
        fdlTotalEvent.top = new FormAttachment(cmbCostTotalTrace, margin);
        wlTotalEvent.setLayoutData(fdlTotalEvent);

        cmbCostTotalEvent = new Combo(wCostGroup, SWT.READ_ONLY);
        props.setLook(cmbCostTotalEvent);
        cmbCostTotalEvent.addModifyListener(lsMod);
        cmbCostTotalEvent.setItems(nombresDeColumnas);
        FormData fdTotalEvent = new FormData();
        fdTotalEvent.left = new FormAttachment(middle, 0);
        fdTotalEvent.right = new FormAttachment(100, 0);
        fdTotalEvent.top = new FormAttachment(cmbCostTotalTrace, margin);
        cmbCostTotalEvent.setLayoutData(fdTotalEvent);

        // para currency
        Label wlCurrency = new Label(wCostGroup, SWT.RIGHT);
        wlCurrency.setText(BaseMessages.getString(PKG, "XESPlugin.Tab.Cost.Currency"));
        props.setLook(wlCurrency);
        FormData fdlCurrency = new FormData();
        fdlCurrency.left = new FormAttachment(0, 0);
        fdlCurrency.right = new FormAttachment(middle, -margin);
        fdlCurrency.top = new FormAttachment(cmbCostTotalEvent, margin);
        wlCurrency.setLayoutData(fdlCurrency);

        cmbCostCurrency = new Combo(wCostGroup, SWT.READ_ONLY);
        props.setLook(cmbCostCurrency);
        cmbCostCurrency.addModifyListener(lsMod);
        cmbCostCurrency.setItems(nombresDeColumnas);
        FormData fdCurrency = new FormData();
        fdCurrency.left = new FormAttachment(middle, 0);
        fdCurrency.right = new FormAttachment(100, 0);
        fdCurrency.top = new FormAttachment(cmbCostTotalEvent, margin);
        cmbCostCurrency.setLayoutData(fdCurrency);

        FormData fdCostGroup = new FormData();
        fdCostGroup.left = new FormAttachment(0, margin);
        fdCostGroup.top = new FormAttachment(0, margin);
        fdCostGroup.right = new FormAttachment(100, -margin);
        wCostGroup.setLayoutData(fdCostGroup);

        FormData fdCostComp = new FormData();
        fdCostComp.left = new FormAttachment(0, 0);
        fdCostComp.top = new FormAttachment(0, 0);
        fdCostComp.right = new FormAttachment(100, 0);
        fdCostComp.bottom = new FormAttachment(100, 0);
        wCostComp.setLayoutData(fdCostComp);

        wCostComp.layout();
        wCostTab.setControl(wCostComp);
        props.setLook(wCostComp);
    }

    private void field (){
        wFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
        wFieldsTab.setText( BaseMessages.getString( PKG, "XESPlugin.Tab.Fields.Tabname" ) );

        FormLayout fieldsLayout = new FormLayout();
        fieldsLayout.marginWidth = Const.FORM_MARGIN;
        fieldsLayout.marginHeight = Const.FORM_MARGIN;

        wFieldsComp = new Composite( wTabFolder, SWT.NONE );
        wFieldsComp.setLayout( fieldsLayout );
        props.setLook( wFieldsComp );

        final int FieldsRows = 10;
        //Creando columnas
        colinf =
                new ColumnInfo[] {
                        new ColumnInfo(BaseMessages.getString( PKG, "XESPlugin.FieldName.Column"),
                                ColumnInfo.COLUMN_TYPE_CCOMBO, nombresDeColumnas,false),
                        new ColumnInfo( BaseMessages.getString( PKG, "XESPlugin.Name.Column" ),
                                ColumnInfo.COLUMN_TYPE_TEXT, new String [] { "" }, false),
                        new ColumnInfo( BaseMessages.getString( PKG, "XESPlugin.Type.Column" ),
                                ColumnInfo.COLUMN_TYPE_CCOMBO, Tipos, false ),
                        new ColumnInfo( BaseMessages.getString( PKG, "XESPlugin.Dat.Column"),
                                ColumnInfo.COLUMN_TYPE_CCOMBO, Datos, false)};

        colinf [0].setToolTip(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Entrys"));
        colinf [1].setToolTip(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Fiiledname"));
        colinf [2].setToolTip(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Typename"));
        colinf [3].setToolTip(BaseMessages.getString(PKG, "XESPlugin.Tooltip.Dataname"));

        colinf[1].setUsingVariables( true );
        //Creando la tabla
        wFields =
                new TableView( transMeta, wFieldsComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod,
                        props );

        fdFields = new FormData();
        fdFields.left = new FormAttachment( 0, 0 );
        fdFields.top = new FormAttachment( 0, 0 );
        fdFields.right = new FormAttachment( 100, 0 );
        fdFields.bottom = new FormAttachment( 100, -margin );
        wFields.setLayoutData( fdFields );

        FormData fdFieldsComp = new FormData();
        fdFieldsComp.left = new FormAttachment( 0, 0 );
        fdFieldsComp.top = new FormAttachment( 0, 0 );
        fdFieldsComp.right = new FormAttachment( 100, 0 );
        fdFieldsComp.bottom = new FormAttachment( 100, 0 );
        wFieldsComp.setLayoutData( fdFieldsComp );

        wFieldsComp.layout();
        wFieldsTab.setControl( wFieldsComp );

        FormData fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment( 0, 0 );
        fdTabFolder.top = new FormAttachment( wStepname, margin );
        fdTabFolder.right = new FormAttachment( 100, 0 );
        fdTabFolder.bottom = new FormAttachment( 100, -50 );
        wTabFolder.setLayoutData( fdTabFolder );

    }

    //Metodo que se hace cargo de guardar la direccion de salida del xes
    private void OutputPathShow() {
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterExtensions(new String[]{"*.xes"});
        String savetarget = dialog.open();
        if (savetarget != null) {
            this.wRutaSalida.setText(savetarget);
        }
    }

    /**
     * This helper method puts the step configuration stored in the meta object
     * and puts it into the dialog controls.
     */
    private void populateDialog() {
        wStepname.selectAll();
        if (meta.getMapa_vista() != null) {
            // se deja hacer asi puesto que los combobox son de tipo read-only
            if (meta.getMapa_vista().get("InstanciaProceso") != null) {
                cmbIProceso.setText(meta.getMapa_vista().get("InstanciaProceso"));
            }
            if (meta.getMapa_vista().get("Actividad") != null) {
                cmbActividad.setText(meta.getMapa_vista().get("Actividad"));
            }
            if (meta.getMapa_vista().get("CicloVida") != null) {
                cmbCicloVida.setText(meta.getMapa_vista().get("CicloVida"));
            }
            if (meta.getMapa_vista().get("Activity_instans") != null) {
                cmbActivityInstans.setText(meta.getMapa_vista().get("Activity_instans"));
            }
            if (meta.getMapa_vista().get("Recurso") != null) {
                cmbRecurso.setText(meta.getMapa_vista().get("Recurso"));
            }
            if (meta.getMapa_vista().get("Rol") != null) {
                cmbRol.setText(meta.getMapa_vista().get("Rol"));
            }
            if (meta.getMapa_vista().get("Grupo") != null) {
                cmbGrupo.setText(meta.getMapa_vista().get("Grupo"));
            }
            if (meta.getMapa_vista().get("RutaSalida") != null) {
                wRutaSalida.setText(meta.getMapa_vista().get("RutaSalida"));
            }
            if (meta.getMapa_vista().get("MarcaTiempo") != null) {
                cmbTimeStamp.setText(meta.getMapa_vista().get("MarcaTiempo"));
            }
            if (meta.getMapa_vista().get("RegexMarcaTiempo") != null) {
                wNuevaRegex1.setEnabled(true);
                wlNuevaRegex.setEnabled(true);
                btnNuevaRegex.setSelection(true);
                wNuevaRegex1.setText(meta.getMapa_vista().get("RegexMarcaTiempo"));

                wlRegex.setEnabled(false);
                cmbregexTimeStamp.setEnabled(false);
            }
            if (meta.getMapa_vista().get("Nivel") != null) {
                cmbLevel.setText(meta.getMapa_vista().get("Nivel"));
            }
            if (meta.getMapa_vista().get("IDPadre") != null) {
                cmbParentID.setText(meta.getMapa_vista().get("IDPadre"));
            }
            if (meta.getMapa_vista().get("ID") != null) {
                cmbID.setText(meta.getMapa_vista().get("ID"));
            }
            if (meta.getMapa_vista().get("Moneda") != null) {
                cmbCostCurrency.setText(meta.getMapa_vista().get("Moneda"));
            }
            if (meta.getMapa_vista().get("TraceTotal") != null) {
                cmbCostTotalTrace.setText(meta.getMapa_vista().get("TraceTotal"));
            }
            if (meta.getMapa_vista().get("EventoTotal") != null) {
                cmbCostTotalEvent.setText(meta.getMapa_vista().get("EventoTotal"));
            }
        }
        //codigo para escribir las celdas de la tabla
        if(meta.getNewatr() != null) {
            for (int k = 0; k < meta.getNewatr().size(); k++) {
                XESPluginField f = meta.getNewatr().get(k);

                TableItem item = wFields.table.getItem(k);
                item.setText( 1, Const.NVL( f.getName(), "" ) );
                item.setText( 2, Const.NVL( f.getFieldName(), "" ) );
                item.setText( 3, Const.NVL( f.getTypename(), "" ) );
                item.setText( 4, Const.NVL( f.getDatodname(), "" ) );
            }
        }
    }

    //**
    // Metodo para Adicionar Atributo desde la tabla
    //**
    private void getInfo(){
         numFields = wFields.nrNonEmpty();
        for(int k=0;k<numFields;k++){

            TableItem item=wFields.getNonEmpty(k);
            XESPluginField field = new XESPluginField(item.getText(1),item.getText(2), item.getText(3), item.getText(4));
            newatrib.put(cont, field);
            cont++;
        }
    }

    /**
     * Called when the user cancels the dialog.
     */
    private void cancel() {
        // The "stepname" variable will be the return value for the open() method.
        // Setting to null to indicate that dialog was cancelled.
        stepname = null;
        // Restoring original "changed" flag on the met aobject
        meta.setChanged(changed);
        // close the SWT dialog window
        dispose();
    }

    /**
     * Called when the user confirms the dialog
     */
    private void ok() {
        // The "stepname" variable will be the return value for the open() method.
        // Setting to step name from the dialog control
        stepname = wStepname.getText();

//        test obtiene valor -1 cuando no hay seleccion de campos hecha
//        int test = cmbCicloVida.getSelectionIndex();

        MessageBox dialog_error = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);

        //metodo para adicionar nuvos atributos
        getInfo();

        //Validaciones de datos
        Map<String, String> map_vista = new HashMap<>();
        int response_code = 0;

        if (cmbIProceso.getSelectionIndex() != -1) {
            map_vista.put("InstanciaProceso", cmbIProceso.getItem(cmbIProceso.getSelectionIndex()));
        } else {
            dialog_error.setText(BaseMessages.getString(PKG, "XESPlugin.Messages.Shell.EmptyColumn.Title"));
            dialog_error.setMessage(BaseMessages.getString(PKG, "XESPlugin.Messages.Shell.CaseEmpty.Description"));
            response_code = dialog_error.open();
        }

        if (cmbTimeStamp.getSelectionIndex() != -1) {
            map_vista.put("MarcaTiempo", cmbTimeStamp.getItem(cmbTimeStamp.getSelectionIndex()));

            if (cmbregexTimeStamp.isEnabled()) {
                if (cmbregexTimeStamp.getSelectionIndex() != -1) {
                    map_vista.put("RegexMarcaTiempo", cmbregexTimeStamp.getItem(cmbregexTimeStamp.getSelectionIndex()));
                }else{
                    dialog_error.setText(BaseMessages.getString(PKG, "XESPlugin.Messages.Shell.GenericError.Title"));
                    dialog_error.setMessage(BaseMessages.getString(PKG, "XESPlugin.Messages.Shell.TimestampRegexNotSpecified.Description"));
                    response_code = dialog_error.open();
                }
            } else {
                if (wNuevaRegex1.getText() != null) {
                    map_vista.put("RegexMarcaTiempo", wNuevaRegex1.getText().trim());
                }else{
                    dialog_error.setText(BaseMessages.getString(PKG, "XESPlugin.Messages.Shell.GenericError.Title"));
                    dialog_error.setMessage(BaseMessages.getString(PKG, "XESPlugin.Messages.Shell.TimestampRegexNotSpecified.Description"));
                    response_code = dialog_error.open();
                }
            }
        }

        if (cmbActividad.getSelectionIndex() != -1) {
            map_vista.put("Actividad", cmbActividad.getItem(cmbActividad.getSelectionIndex()));
        }else{
            dialog_error.setText(BaseMessages.getString(PKG, "XESPlugin.Messages.Shell.EmptyColumn.Title"));
            dialog_error.setMessage(BaseMessages.getString(PKG, "XESPlugin.Messages.Shell.ActivityEmpty.Description"));
            response_code = dialog_error.open();
        }
        if (cmbCicloVida.getSelectionIndex() != -1) {
            map_vista.put("CicloVida", cmbCicloVida.getItem(cmbCicloVida.getSelectionIndex()));
        }
        if (cmbActivityInstans.getSelectionIndex() != -1){
            map_vista.put("Activity_instans", cmbActivityInstans.getItem(cmbActivityInstans.getSelectionIndex()));
        }
        if (cmbActivityInstans.getSelectionIndex() != -1) {
            map_vista.put("Activity_Instans", cmbActivityInstans.getItem(cmbActivityInstans.getSelectionIndex()));
        }
        if (cmbRecurso.getSelectionIndex() != -1) {
            map_vista.put("Recurso", cmbRecurso.getItem(cmbRecurso.getSelectionIndex()));
        }
        if (cmbRol.getSelectionIndex() != -1) {
            map_vista.put("Rol", cmbRol.getItem(cmbRol.getSelectionIndex()));
        }
        if (cmbGrupo.getSelectionIndex() != -1) {
            map_vista.put("Grupo", cmbGrupo.getItem(cmbGrupo.getSelectionIndex()));
        }
        if (cmbLevel.getSelectionIndex() != -1) {
            map_vista.put("Nivel", cmbLevel.getItem(cmbLevel.getSelectionIndex()));
        }
        if (cmbParentID.getSelectionIndex() != -1) {
            map_vista.put("IDPadre", cmbParentID.getItem(cmbParentID.getSelectionIndex()));
            if (cmbID.getSelectionIndex() == -1) {
                dialog_error.setText(BaseMessages.getString(PKG, "XESPlugin.Messages.Shell.EmptyColumn.Title"));
                dialog_error.setMessage(BaseMessages.getString(PKG, "XESPlugin.Messages.Shell.ParentIDbutNoIDExtension.Description"));
                response_code = dialog_error.open();
            }
        }
        if (cmbID.getSelectionIndex() != -1) {
            map_vista.put("ID", cmbID.getItem(cmbID.getSelectionIndex()));
        }
        if (cmbCostCurrency.getSelectionIndex() != -1) {
            map_vista.put("Moneda", cmbCostCurrency.getItem(cmbCostCurrency.getSelectionIndex()));
        }
        if (cmbCostTotalTrace.getSelectionIndex() != -1) {
            map_vista.put("TraceTotal", cmbCostTotalTrace.getItem(cmbCostTotalTrace.getSelectionIndex()));
        }
        if (cmbCostTotalEvent.getSelectionIndex() != -1) {
            map_vista.put("EventoTotal", cmbCostTotalEvent.getItem(cmbCostTotalEvent.getSelectionIndex()));
        }
        if (this.wRutaSalida.getText() != null) {
            map_vista.put("RutaSalida", this.wRutaSalida.getText().trim());
        }
            //Guardando mapas de entrada de datos
        meta.setMapa_vista(map_vista);
        meta.setNewatr(newatrib);
        meta.setCont(cont);
        // close the SWT dialog window

        if (response_code != 32) //code 32 para boton aceptar
            dispose();
    }
}

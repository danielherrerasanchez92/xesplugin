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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.core.Props;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI.
 * <p>
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 * <p>
 * This class is the implementation of StepDialogInterface.
 * Classes implementing this interface need to:
 * <p>
 * - build and open a SWT dialog displaying the step's settings (stored in the step's meta object)
 * - write back any changes the user makes to the step's meta object
 * - report whether the user changed any settings when confirming the dialog
 */
public class DemoStepDialog extends BaseStepDialog implements StepDialogInterface {

    /**
     * The PKG member is used when looking up internationalized strings.
     * The properties file with localized keys is expected to reside in
     * {the package of the class specified}/messages/messages_{locale}.properties
     */

    // this is the object the stores the step's settings
    // the dialog reads the settings from it when opening
    // the dialog writes the settings to it when confirmed
    private DemoStepMeta meta;

    private Text wRutaSalida;
    private Listener lsRuta;

    private CTabFolder wTabFolder;

    private CTabItem wProcessTab;
    private Composite wProcessComp;
    private Group wProcessGroup;

    private Combo cmbIProceso;
    private Combo cmbActividad;
    private Combo cmbCicloVida;

    private CTabItem wTimeTab;
    private Composite wTimeComp;
    private Group wTimeGroup;

    private Combo cmbTimeStamp;
    private Label wlRegex;
    private Combo cmbregexTimeStamp;
    private Text wNuevaRegex;
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

    private CTabItem wResourceTab;
    private Composite wResourceComp;
    private Group wResourceGroup;

    private Combo cmbRecurso;
    private Combo cmbRol;
    private Combo cmbGrupo;

    private String[] nombresDeColumnas;
    private int middle;
    private int margin;
    private ModifyListener lsMod;

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
    public DemoStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
        super(parent, (BaseStepMeta) in, transMeta, sname);
        meta = (DemoStepMeta) in;
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
        shell.setText("XESPlugin");

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
        wlStepname.setText("Stepname:");
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
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        //para ruta de salida
        Label wlRutaSalida = new Label(shell, SWT.RIGHT);
        wlRutaSalida.setText("Output path:");
        props.setLook(wlRutaSalida);
        FormData fdlRutaSalida = new FormData();
        fdlRutaSalida.left = new FormAttachment(0, 0);
        fdlRutaSalida.top = new FormAttachment(wStepname, margin);
        fdlRutaSalida.right = new FormAttachment(middle, -margin);
        wlRutaSalida.setLayoutData(fdlRutaSalida);

        Button btnRuta = new Button(shell, SWT.PUSH | SWT.CENTER);
        btnRuta.setText("Browse");
        props.setLook(btnRuta);
        FormData fdBtnRuta = new FormData();
        fdBtnRuta.right = new FormAttachment(100, 0);
        fdBtnRuta.top = new FormAttachment(wStepname, margin);
        btnRuta.setLayoutData(fdBtnRuta);

        wRutaSalida = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
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

        AddProcessTab();
        AddTimeTab();
        AddResourceTab();

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
        wOK.setText("Ok");
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText("Cancel");

        BaseStepDialog.positionBottomRightButtons(shell, new Button[]{wOK, wCancel}, margin, wTabFolder);
//        BaseStepDialog.positionBottomButtons(shell, new Button[]{wOK, wCancel}, margin, wTabFolder);

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
        cmbTimeStamp.addSelectionListener(lsDef);
        cmbregexTimeStamp.addSelectionListener(lsDef);
        cmbCicloVida.addSelectionListener(lsDef);

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

    private void AddProcessTab() {
        //*************
        //Process tab *
        //*************
        wProcessTab = new CTabItem(wTabFolder, SWT.NONE);
        wProcessTab.setText("Process");

        wProcessComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wProcessComp);

        FormLayout processLayout = new FormLayout();
        processLayout.marginWidth = 3;
        processLayout.marginHeight = 3;
        wProcessComp.setLayout(processLayout);

        wProcessGroup = new Group(wProcessComp, SWT.SHADOW_NONE);
        props.setLook(wProcessGroup);
        wProcessGroup.setText("Process related columns:");

        FormLayout processgroupLayout = new FormLayout();
        processgroupLayout.marginWidth = 10;
        processgroupLayout.marginHeight = 10;
        wProcessGroup.setLayout(processgroupLayout);

        // para instancia de proceso
        Label wlIP = new Label(wProcessGroup, SWT.RIGHT);  // y en estos iba el shell
        wlIP.setText("Case:");
        props.setLook(wlIP);
        FormData fdlIP = new FormData();
        fdlIP.left = new FormAttachment(0, 0);
        fdlIP.right = new FormAttachment(middle, -margin);
        fdlIP.top = new FormAttachment(0, margin); //aqui tenia el wStepname
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
        wlActividad.setText("Activity:");
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
        wlCicloVida.setText("Lifecycle:");
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

    private void AddTimeTab(){
        //************************
        //TimeStamp + Regex tabs *
        //************************
        wTimeTab = new CTabItem(wTabFolder, SWT.NONE);
        wTimeTab.setText("Timestamp");

        wTimeComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wTimeComp);

        FormLayout timeLayout = new FormLayout();
        timeLayout.marginWidth = 3;
        timeLayout.marginHeight = 3;
        wTimeComp.setLayout(timeLayout);

        wTimeGroup = new Group(wTimeComp, SWT.SHADOW_NONE);
        props.setLook(wTimeGroup);
        wTimeGroup.setText("Timestamp related columns: ");
        FormLayout timegroupLayout = new FormLayout();
        timegroupLayout.marginWidth = 10;
        timegroupLayout.marginHeight = 10;
        wTimeGroup.setLayout(timegroupLayout);

        //para marca de tiempo
        Label wlTimeStamp = new Label(wTimeGroup, SWT.RIGHT);
        wlTimeStamp.setText("Timestamp:");
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
        wlRegex.setText("Timestamp format:");
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
        btnNuevaRegex.setText("New format?");
        props.setLook(btnNuevaRegex);
        FormData fdcNuevaRegex = new FormData();
        fdcNuevaRegex.left = new FormAttachment(middle, 0);
        fdcNuevaRegex.right = new FormAttachment(100, 0);
        fdcNuevaRegex.top = new FormAttachment(cmbregexTimeStamp, margin);
        btnNuevaRegex.setLayoutData(fdcNuevaRegex);

        wlNuevaRegex = new Label(wTimeGroup, SWT.RIGHT);
        wlNuevaRegex.setText("New format:");
        props.setLook(wlNuevaRegex);
        FormData fdlNuevaRegex = new FormData();
        fdlNuevaRegex.left = new FormAttachment(0, 0);
        fdlNuevaRegex.right = new FormAttachment(middle, -margin);
        fdlNuevaRegex.top = new FormAttachment(btnNuevaRegex, margin);
        wlNuevaRegex.setLayoutData(fdlNuevaRegex);
        wlNuevaRegex.setEnabled(false);

        wNuevaRegex = new Text(wTimeGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wNuevaRegex);
        wNuevaRegex.addModifyListener(lsMod);
        FormData fdNuevaRegex = new FormData();
        fdNuevaRegex.left = new FormAttachment(middle, 0);
        fdNuevaRegex.top = new FormAttachment(btnNuevaRegex, margin);
        fdNuevaRegex.right = new FormAttachment(100, 0);
        wNuevaRegex.setLayoutData(fdNuevaRegex);
        wNuevaRegex.setEnabled(false);

        //listener para adicionar una nueva expresion regular
        lsNuevaRegex = new Listener() {
            public void handleEvent(Event e) {
                //codigo para nuevo regex
                if (wNuevaRegex.isEnabled()) {
                    wNuevaRegex.setEnabled(false);
                    wlNuevaRegex.setEnabled(false);
                    cmbregexTimeStamp.setEnabled(true);
                    wlRegex.setEnabled(true);
                } else {
                    wNuevaRegex.setEnabled(true);
                    wlNuevaRegex.setEnabled(true);
                    cmbregexTimeStamp.setEnabled(false);
                    wlRegex.setEnabled(false);
                }
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

    private void AddResourceTab(){
        //***************
        //Resources tab *
        //***************
        wResourceTab = new CTabItem(wTabFolder, SWT.NONE);
        wResourceTab.setText("Resources");

        wResourceComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wResourceComp);

        FormLayout resourceLayout = new FormLayout();
        resourceLayout.marginWidth = 3;
        resourceLayout.marginHeight = 3;
        wResourceComp.setLayout(resourceLayout);

        wResourceGroup = new Group(wResourceComp, SWT.SHADOW_NONE);
        props.setLook(wResourceGroup);
        wResourceGroup.setText("Resources related columns: ");
        FormLayout resourcesgroupLayout = new FormLayout();
        resourcesgroupLayout.marginWidth = 10;
        resourcesgroupLayout.marginHeight = 10;
        wResourceGroup.setLayout(resourcesgroupLayout);

        //para Recurso
        Label wlRecurso = new Label(wResourceGroup, SWT.RIGHT);
        wlRecurso.setText("Resource:");
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
        wlRol.setText("Role:");
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
        wlGrupo.setText("Group:");
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
                wNuevaRegex.setEnabled(true);
                wlNuevaRegex.setEnabled(true);
                btnNuevaRegex.setSelection(true);
                wNuevaRegex.setText(meta.getMapa_vista().get("RegexMarcaTiempo"));

                wlRegex.setEnabled(false);
                cmbregexTimeStamp.setEnabled(false);
            }
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
        // Setting the  settings to the meta object

//        test obtiene valor -1 cuando no hay seleccion de campos hecha
//        int test = cmbCicloVida.getSelectionIndex();

        Map<String, String> map_vista = new HashMap<>();
        if (cmbIProceso.getSelectionIndex() != -1) {
            map_vista.put("InstanciaProceso", cmbIProceso.getItem(cmbIProceso.getSelectionIndex()));
        }
        if (cmbTimeStamp.getSelectionIndex() != -1) {
            map_vista.put("MarcaTiempo", cmbTimeStamp.getItem(cmbTimeStamp.getSelectionIndex()));
        }
        if (cmbregexTimeStamp.isEnabled()) {
            if (cmbregexTimeStamp.getSelectionIndex() != -1) {
                map_vista.put("RegexMarcaTiempo", cmbregexTimeStamp.getItem(cmbregexTimeStamp.getSelectionIndex()));
            }
        } else {
            if (wNuevaRegex.getText() != null) {
                map_vista.put("RegexMarcaTiempo", wNuevaRegex.getText().trim());
            }
        }
        if (cmbActividad.getSelectionIndex() != -1) {
            map_vista.put("Actividad", cmbActividad.getItem(cmbActividad.getSelectionIndex()));
        }
        if (cmbCicloVida.getSelectionIndex() != -1) {
            map_vista.put("CicloVida", cmbCicloVida.getItem(cmbCicloVida.getSelectionIndex()));
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
        if (this.wRutaSalida.getText() != null) {
            map_vista.put("RutaSalida", this.wRutaSalida.getText().trim());
        }

        meta.setMapa_vista(map_vista);

        // close the SWT dialog window
        dispose();
    }
}

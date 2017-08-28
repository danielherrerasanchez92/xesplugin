package org.pentaho.di.sdk.samples.steps.demo;

/**
 * Created by Norbe on 5/17/2017.
 */

//Clase que guarda los objetos de tipo fila (nuevos atributos)

public class XESPluginField implements Cloneable {

    private String name;
    private String fieldname;
    private String typename;
    private String datodname;
    private boolean classifier;

    public XESPluginField( String name,String fieldname, String typename, String datodname) {
        this.name = name;
        this.fieldname = fieldname;
        this.typename = typename;
        this.datodname = datodname;
    }
    public XESPluginField( String name,String fieldname, String typename, String datodname, boolean classifier) {
        this.name = name;
        this.fieldname = fieldname;
        this.typename = typename;
        this.datodname = datodname;
        this.classifier = classifier;
    }

    public XESPluginField() {
    }

    public int compare( Object obj ) {
        XESPluginField field = (XESPluginField) obj;

        return fieldname.compareTo( field.getFieldName() );
    }

    public boolean equal( Object obj ) {
        XESPluginField field = (XESPluginField) obj;

        return fieldname.equals( field.getFieldName() );
    }

    public Object clone() {
        try {
            Object retval = super.clone();
            return retval;
        } catch ( CloneNotSupportedException e ) {
            return null;
        }
    }

    public String getFieldName() {
        return fieldname;
    }

    public void setFieldName( String fieldname ) {
        this.fieldname = fieldname;
    }

    /**
     * @return Returns the typename.
     */
    public String getTypename() {
        return typename;
    }

    /**
     * @param typename
     *          The elementName to set.
     */
    public void setTypename( String typename ) {
        this.typename = typename;
    }

    public String getDatodname() {
        return datodname;
    }

    public void setDatodname(String datodname) {
        this.datodname = datodname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isClassifier() {
        return classifier;
    }

    public void setClassifier(boolean classifier) {
        this.classifier = classifier;
    }

}

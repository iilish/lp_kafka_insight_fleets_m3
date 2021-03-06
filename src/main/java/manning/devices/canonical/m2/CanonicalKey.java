/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package manning.devices.canonical.m2;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class CanonicalKey extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 4949592468121283821L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"CanonicalKey\",\"namespace\":\"manning.devices.canonical.m2\",\"fields\":[{\"name\":\"uuid\",\"type\":[\"null\",\"string\"]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence uuid;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public CanonicalKey() {}

  /**
   * All-args constructor.
   */
  public CanonicalKey(java.lang.CharSequence uuid) {
    this.uuid = uuid;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return uuid;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: uuid = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'uuid' field.
   */
  public java.lang.CharSequence getUuid() {
    return uuid;
  }

  /**
   * Sets the value of the 'uuid' field.
   * @param value the value to set.
   */
  public void setUuid(java.lang.CharSequence value) {
    this.uuid = value;
  }

  /**
   * Creates a new CanonicalKey RecordBuilder.
   * @return A new CanonicalKey RecordBuilder
   */
  public static manning.devices.canonical.m2.CanonicalKey.Builder newBuilder() {
    return new manning.devices.canonical.m2.CanonicalKey.Builder();
  }
  
  /**
   * Creates a new CanonicalKey RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new CanonicalKey RecordBuilder
   */
  public static manning.devices.canonical.m2.CanonicalKey.Builder newBuilder(manning.devices.canonical.m2.CanonicalKey.Builder other) {
    return new manning.devices.canonical.m2.CanonicalKey.Builder(other);
  }
  
  /**
   * Creates a new CanonicalKey RecordBuilder by copying an existing CanonicalKey instance.
   * @param other The existing instance to copy.
   * @return A new CanonicalKey RecordBuilder
   */
  public static manning.devices.canonical.m2.CanonicalKey.Builder newBuilder(manning.devices.canonical.m2.CanonicalKey other) {
    return new manning.devices.canonical.m2.CanonicalKey.Builder(other);
  }
  
  /**
   * RecordBuilder for CanonicalKey instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<CanonicalKey>
    implements org.apache.avro.data.RecordBuilder<CanonicalKey> {

    private java.lang.CharSequence uuid;

    /** Creates a new Builder */
    private Builder() {
      super(manning.devices.canonical.m2.CanonicalKey.SCHEMA$);
    }
    
    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(manning.devices.canonical.m2.CanonicalKey.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.uuid)) {
        this.uuid = data().deepCopy(fields()[0].schema(), other.uuid);
        fieldSetFlags()[0] = true;
      }
    }
    
    /**
     * Creates a Builder by copying an existing CanonicalKey instance
     * @param other The existing instance to copy.
     */
    private Builder(manning.devices.canonical.m2.CanonicalKey other) {
            super(manning.devices.canonical.m2.CanonicalKey.SCHEMA$);
      if (isValidValue(fields()[0], other.uuid)) {
        this.uuid = data().deepCopy(fields()[0].schema(), other.uuid);
        fieldSetFlags()[0] = true;
      }
    }

    /**
      * Gets the value of the 'uuid' field.
      * @return The value.
      */
    public java.lang.CharSequence getUuid() {
      return uuid;
    }

    /**
      * Sets the value of the 'uuid' field.
      * @param value The value of 'uuid'.
      * @return This builder.
      */
    public manning.devices.canonical.m2.CanonicalKey.Builder setUuid(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.uuid = value;
      fieldSetFlags()[0] = true;
      return this; 
    }

    /**
      * Checks whether the 'uuid' field has been set.
      * @return True if the 'uuid' field has been set, false otherwise.
      */
    public boolean hasUuid() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'uuid' field.
      * @return This builder.
      */
    public manning.devices.canonical.m2.CanonicalKey.Builder clearUuid() {
      uuid = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    @Override
    public CanonicalKey build() {
      try {
        CanonicalKey record = new CanonicalKey();
        record.uuid = fieldSetFlags()[0] ? this.uuid : (java.lang.CharSequence) defaultValue(fields()[0]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  private static final org.apache.avro.io.DatumWriter
    WRITER$ = new org.apache.avro.specific.SpecificDatumWriter(SCHEMA$);  

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, org.apache.avro.specific.SpecificData.getEncoder(out));
  }

  private static final org.apache.avro.io.DatumReader
    READER$ = new org.apache.avro.specific.SpecificDatumReader(SCHEMA$);  

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, org.apache.avro.specific.SpecificData.getDecoder(in));
  }

}

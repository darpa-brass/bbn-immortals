(ns vuisis.immortals.rdf-jena
  "Convert the deployment instance into RDF updates.
  This uses https://jena.apache.org/documentation/javadoc/jena/
  "
  (:require
    [clojure.string :as string]
    [clojure.tools.logging :as log]
    [vuisis.immortals.convert :as cvt])
  (:import
    [java.io BufferedReader
      ByteArrayInputStream IOException InputStream InputStreamReader]
    [org.objectweb.asm Type]
    [java.util Calendar Date]
    [org.apache.jena.rdf.model
      Model RDFNode Resource Statement StmtIterator
      ModelFactory Literal]
    [org.apache.jena.sparql.vocabulary FOAF]
    [org.apache.jena.vocabulary RDF]
    [org.apache.jena.datatypes.xsd XSDDatatype]
    [org.apache.jena.query
      Query QueryExecution QueryExecutionFactory
      QueryFactory QuerySolution
      ResultSet ResultSetFactory ResultSetFormatter]
    [org.apache.jena.ontology OntModelSpec]
    ))

(def namespace "http://securboration.com/immortals/r1.0")
(def ontology-model (ModelFactory/createOntologyModel OntModelSpec/OWL_MEM))

(defrecord JenaModel [instance]
  cvt/RecorderProtocol
  (create-resource! [this urid]
    (try
      (.createResource (:instance this) (str namespace "#" urid))
      (catch java.lang.IllegalArgumentException ex
        (log/error "bad resource argument " urid "\n" ex))))
  (add-fact! [this subject predicate object]
    (try
      (.addProperty subject predicate (str object))
      (catch java.lang.IllegalArgumentException ex
        (log/error "bad property argument " subject " " predicate " " object ))
      (catch java.lang.NullPointerException ex
        (log/error "null pointer " subject " " predicate " " object ))
      (catch Exception ex
        (log/error "general exception " subject " " predicate " " object ))))
  (dump [this]
    (.write (:instance this) System/out "TURTLE")))

(defn make-model []
  (log/debug "create rdf jena model")
  (->JenaModel (ModelFactory/createDefaultModel)))

(def asm-primitive-type-map
  { Type/BOOLEAN_TYPE :boolean
    Type/CHAR_TYPE    :char
    Type/DOUBLE_TYPE  :double
    Type/FLOAT_TYPE   :float
    Type/INT_TYPE     :int
    Type/LONG_TYPE    :long
    Type/SHORT_TYPE   :short

    ;; less primitive types
    (Type/getType String)   :string
    (Type/getType (Class/forName "[B")) :base64Binary ;;  byte[]) did not work
    (Type/getType Date)     :date
    (Type/getType Calendar) :dateTime
    })

(defn make-primitive-type-map [model]
  (reduce-kv
    (fn [m k v] (assoc m k (.getResource model (.getUri v))) )
    {}
  { :boolean  XSDDatatype/XSDboolean
    :char     XSDDatatype/XSDunsignedByte
    :double   XSDDatatype/XSDdouble
    :float    XSDDatatype/XSDfloat
    :int      XSDDatatype/XSDint
    :long     XSDDatatype/XSDlong
    :short    XSDDatatype/XSDshort
    :string   XSDDatatype/XSDstring
    :abyte    XSDDatatype/XSDbase64Binary
    :date     XSDDatatype/XSDdate
    :calendar XSDDatatype/XSDdateTime }))

(defn make-deply-prop-map [model]
  (reduce-kv
    (fn [m k v] (assoc m k (.getResource model (.getUri v))) )
    {}
  { :type  RDF/type
    :name  (.createProperty model )
    :uuid
    :member
    :class
    :value
  }))

(ns vuisis.immortals.examples-jena
  "Convert the deployment instance into RDF updates.
  This implements https://github.com/castagna/jena-examples
  "
  (:require
    [clojure.string :as string]
    [clojure.tools.logging :as log])
  (:import
    [org.apache.jena.rdf.model
      Model RDFNode Resource Statement StmtIterator
      ModelFactory Literal]
    [org.apache.jena.sparql.vocabulary FOAF]
    [org.apache.jena.vocabulary RDF]
    [java.io BufferedReader
      ByteArrayInputStream IOException InputStream InputStreamReader]
    [org.apache.jena.query
      Query QueryExecution QueryExecutionFactory
      QueryFactory QuerySolution
      ResultSet ResultSetFactory ResultSetFormatter]))

(defn reveal
  "print out some interesting stuff about the triple"
  [triple]
  (let [s (.getSubject triple)
        p (.getPredicate triple)
        o (.getObject triple)]
      (cond
        (.isURIResource s)  "URI"
        (.isAnon s)  "blank" )
      (cond
        (.isURIResource p) " URI ")
      (cond
        (.isURIResource o)  "URI"
        (.isAnon o) "blank"
        (.isLiteral o)  "literal") ))

(defn example-api-01
  "from jena-examples"
  []
  (let [fm (org.apache.jena.util.FileManager/get)
        model (.loadModel fm "ttl/data1.ttl" nil "TURTLE")
        iter (.listStatements model)
        as-seq (iterator-seq iter)]
      (map #(log/info (reveal %1)) as-seq)))

(defn example-api-02
  "from jena-examples"
  []
  (let [model (ModelFactory/createDefaultModel)
        alice (.createResource model "http://example.org/alice" FOAF/Person)
        mailbox (.createResource model "mailto:alice@example.org")
        bob (.createResource model "http://example.org/bob" FOAF/Person)]
    (-> alice
          (.addProperty FOAF/name "Alice")
          (.addProperty FOAF/mbox mailbox)
          (.addProperty FOAF/knows bob))
    (.write model System/out "TURTLE")
    nil))

(defn example-api-03
  "from jena-examples"
  []
  (let [model (ModelFactory/createDefaultModel)
        alice (.createResource model "http://example.org/alice")
        mailbox (.createResource model "mailto:alice@example.org")
        bob (.createResource model "http://example.org/bob")]
    (.add model alice RDF/type FOAF/Person)
    (.add model alice FOAF/name "Alice")
    (.add model alice FOAF/mbox mailbox)
    (.add model alice FOAF/knows bob)

    (.write model System/out "TURTLE")
    nil))

(defn example-api-04
  "from jena-examples"
  []
  (let [fm (org.apache.jena.util.FileManager/get)
        m-in (.open fm "ttl/data2.ttl")
        reader (BufferedReader. (InputStreamReader. m-in))
        all (ModelFactory/createDefaultModel)]
    (loop [line (.readLine reader)]
      (if (nil? line)
        (.write all System/out "TURTLE") ;; done
        ;; work
        (let [bais (ByteArrayInputStream. (.getBytes line))
              model (ModelFactory/createDefaultModel)]
          (doto model
            (.read bais nil "TURTLE")
            (.write System/out "TURTLE"))
          (doto all
            (.add  model)
            (.setNsPrefixes (.getNsPrefixMap model)))
          (println "-----------------")
          (recur (.readLine reader))))))
  nil)

(defn example-arq-01
  "from jena-examples"
  []
  (let [fm (org.apache.jena.util.FileManager/get)
        model (.loadModel fm "ttl/data1.ttl")
        query-str (str
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                    "SELECT ?name WHERE { "
                    "  ?person foaf:mbox <mailto:alice@example.org> . "
                    "  ?person foaf:name ?name . "
                    "}")
        query (QueryFactory/create query-str)
        qexec (QueryExecutionFactory/create query model)]
    (try
      (let [results (.execSelect qexec)]
        (loop []
          (when (.hasNext results)
            (let [soln (.nextSolution results)
                  name (.getLiteral soln "name")]
              (println (.getString name))
              (recur)))))
      (finally (.close qexec))))
  nil)

(defn example-arq-02
  "from jena-examples"
  []
  (let [fm (org.apache.jena.util.FileManager/get)
        model (.loadModel fm "ttl/data1.ttl")
        query-str "SELECT * { ?s ?p ?o }"
        query (QueryFactory/create query-str)
        qexec (QueryExecutionFactory/create query model)]
    (try
      (let [results (ResultSetFactory/makeRewindable (.execSelect qexec))
            writer (fn [title outer]
              (println "-----" title "-----")
              (outer System/out results)
              (.reset results)) ]
        (writer "XML" #(ResultSetFormatter/outputAsXML %1 %2))
        (writer "Text" #(ResultSetFormatter/out %1 %2))
        (writer "CSV" #(ResultSetFormatter/outputAsCSV %1 %2))
        (writer "TSV" #(ResultSetFormatter/outputAsTSV %1 %2))
        (writer "JSON" #(ResultSetFormatter/outputAsJSON %1 %2)))
      (finally (.close qexec))))
  nil)

(defn example-arq-03
  "a remote SPARQL query"
  []
  (let [apikey (System/getenv "KASABI_API_KEY")
        query-str (str
          "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
          "PREFIX italy: <http://data.kasabi.com/dataset/italy/schema/> "
          "SELECT ?region WHERE { "
          "  ?region rdf:type italy:Region }")
        query (QueryFactory/create query-str)
        qexec (QueryExecutionFactory/createServiceRequest
                "http://api.kasabi.com/dataset/italy/apis/sparql"
                query)]
      (.addParam qexec "apikey" apikey)
      (try
        (let [results (.execSelect qexec)]
          (loop []
            (when (.hasNext results)
              (let [soln (.nextSolution results)
                    region (.getResource soln)]
                (println (.getURI region)))
                (recur))))
        (finally (.close qexec)))))

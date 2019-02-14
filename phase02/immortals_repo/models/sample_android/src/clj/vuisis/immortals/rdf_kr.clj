(ns vuisis.immortals.rdf-kr
  "Convert the deployment instance into RDF updates.
  This uses https://github.com/drlivingston/kr
  "
  (:require
    [clojure.string :as string]
    [clojure.tools.logging :as log]
    [edu.ucdenver.ccp.kr
      [kb :as kb]
      [rdf :as rdf]
      [sparql :as sparql]]
    [edu.ucdenver.ccp.kr.jena
      [kb :as kbimpl]]
      [clj-uuid :as uuid]))

;; (def model (com.hp.hpl.jena.tdb.TDBFactory/createModel "/rdfrepo"))

(defn factory []
  (log/debug "create rdf jena factory")
  (let [kdb (kb/kb :jena-mem)]
    (rdf/register-namespaces kdb
         '(;; types from UML2 ontology
           ("uml" "http://www.omg.org/spec/UML/2.5/")
           ;; types from RDF ontology
           ("rdf" "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
           ;; Friend of a Friend ontology
           ("foaf" "http://xmlns.com/foaf/0.1/")
           ;; tentative types from immortatls
           ("im" "http://vanderbilt.edu/isis/immortals/0.1/")
           ;; an example namespace provided by the iana
           ("ex" "http://www.example.org/")
           ;; from the rdf schema
           ("rdfs" "http://www.w3.org/2000/01/rdf-schema#")
           ;; from the https://en.wikipedia.org/wiki/Web_Ontology_Language
           ("owl" "http://www.w3.org/2002/07/owl#")
           ;; the xml schema
           ("xsd" "http://www.w3.org/2001/XMLSchema#")))))

(defn make-add-fact!
  [kdb]
  (log/debug "make add fact! ")
  (fn add-fact!
    ([[s p o]] (add-fact! s p o))
    ([s p o] 
      (try
        ; (log/info "add args " kdb)
        (rdf/add kdb (list s p o))
        (log/info "triple list added " s " " p " " o)
        (catch IllegalArgumentException ex
          (log/error "could not add fact args " [s p o]))
        (catch Exception ex
          (log/error "general exception " ex " " [s p o]))))))


(defn sample-person
  "Add a sample person to the triple-store."
  [kdb]
  (rdf/add kdb 'im/feisele 'rdf/type 'im/Person)
  (rdf/add kdb '(im/feisele foaf/name "Fred Eisele"))

  (let [fred1 (rdf/ask-rdf kdb nil nil 'im/Person)
        fred2 (sparql/query kdb
          '((?/person rdf/type im/Person)
            (?/person foaf/name ?/name)
            (:optional ((?/person foaf/mbox ?/email)))))]
      (log/info "fred1: " fred1)
      (log/info "fred2: " fred2)))

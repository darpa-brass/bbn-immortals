(ns vuisis.immortals.rdf-seabass
  "Convert the deployment instance into RDF updates.
  This uses https://github.com/drlivingston/kr
  "
  (:require
    [clojure.string :as string]
    [clojure.tools.logging :as log]
    [seabass.core :as sb])
  (:import [com.hp.hpl.jena.rdf.model Model]))

;; (def model (com.hp.hpl.seabass.tdb.TDBFactory/createModel "/rdfrepo"))

(defn factory []
  (log/debug "create rdf seabass factory")
  (let [kdb (kb/kb :seabass-mem)]
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
  (fn ([[l]]
        (try
          (rdf/add kdb l)
          (log/info "triple added " l)
          (catch IllegalArgumentException ex
            (log/error "could not add fact tuple " l))
          (catch Exception ex
            (log/error "general exception " ex " " l))))
      ([s p o]
        (try
          ; (log/info "add args " kdb)
          (rdf/add kdb (list s p o))
          (log/info "triple list added " s " " p " " o)
          (catch IllegalArgumentException ex
            (log/error "could not add fact args " [s p o]))
          (catch Exception ex
            (log/error "general exception " ex " " [s p o]))))))

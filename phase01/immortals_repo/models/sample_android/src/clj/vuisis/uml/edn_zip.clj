

(ns vuisis.uml.edn-zip
  (:require
    [vuisis.uml :as uml]
    [clojure.java.io :as io]
    [clojure.edn :as edn]
    [clojure.walk :as w]
    [clojure.zip :as zip]
    [clojure.string :as string]))


#_(def edn-readers
  {'vuisis.uml.Comment uml/->Comment})


(defn load-edn-file
  "load an edn file using a reader"
  [file-name]
  (parser/parse-file-all file-name))

(def kdb
  (rdf/register-namespaces (kb/kb :sesame-mem)
                     '(;; types from UML2 ontology
                       ("uml" "http://www.omg.org/spec/UML/2.5/")
                       ;; types from RDF ontology
                       ("rdf" "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
                       ;; Friend of a Friend ontology
                       ("foaf" "http://xmlns.com/foaf/0.1/")
                       ;; tentative types from immortatls
                       ("im" "http://vanderbilt.edu/isis/immortals/0.1/"))))

(rdf/add kdb 'im/feisele 'rdf/type 'im/Person)
(rdf/add kdb '(im/feisele foaf/name "Fred Eisele"))

(def fred1 (rdf/ask-rdf kdb nil nil 'im/Person))

(def fred2
  (sparql/query kdb
    '((?/person rdf/type im/Person)
      (?/person foaf/name ?/name)
      (:optional ((?/person foaf/mbox ?/email))))))

;; the use of an EDN input file removes the need for a parser
;; but if you need to load some other grammar, consider...
;;  https://github.com/aphyr/clj-antlr
;; This is probably what I will use for loading java code.
(defn di [] (load-edn-file "resource/deployment_instance.edn"))

(defn store-fact [s p o]
  (println "subject: " s ", pr: " p ", obj: " o))

(defn node-type
  "when passed a node determine its type and return a keyword"
  [node]
  )

(defmulti recorder node-type)
(defmethod recorder :default [node] node)

(defmethod recorder :system
  [system]
  (let [arg-eval (map recorder (:args system))]
    (if (every? string? arg-eval)
      (string/join arg-eval)
      ())))

(defmethod recorder :deployment
  [a]
  ())

(defprotocol fact-store )


(defn transform
  "A script which puts the custom EDN information into RDF.
  The knowledge base (kdb) is traversed with a zipper and
  each fact is added to the facts store.
  The facts store conforms to a protocol supporting add.
  This is being implement with 'tree-seq' but if there
  are memory problems it can be refactored with a 'zipper'.
  The advantage of a zipper is that the parent nodes are
  also available."
  [kdb facts]
  (map recorder
    (tree-seq sequential? seq kdb)))

(defn mk-zipper [root]
  "This zipper traverses all types of collections.
  Maps, an unordered collection, produces nodes that are key-value pairs.
  Sets, also unordered, are simply nodes with those values.
  branch? - checks the current node and "
  (letfn [(branch? [node]
                  (when node
                    (or (and (map? node) (contains? node :inputs))
                        (vector? node))))
          (children [node]
                   (cond
                     (nil? node) nil
                     (map? node) (:inputs node)
                     :else node))
           (make-node [node children]
                    (cond
                      (nil? node) nil
                      (map? node) (assoc node :inputs children)
                      (vector? node) (into [] children)
                      :else node))]
    (zip/zipper branch? children make-node root)))

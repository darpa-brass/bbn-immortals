(ns vuisis.immortals.convert
  "Convert the deployment instance into RDF updates."
  (:require
    [clojure.string :as string]
    [clojure.tools.logging :as log]
    [rewrite-clj
      [parser :as parser]
      [zip :as zip]
      [reader :as reader]]
    [clj-uuid :as uuid])
  (:import
    [org.apache.jena.sparql.vocabulary FOAF]
    [org.apache.jena.vocabulary RDF]))


(defn mock-add-fact! [s p o]
  (log/info "subject: " s ", predicate: " p ", object: " o))


(defn component? [m node-loc] (= :component (get m :tag)))
(defn device? [m node-loc] (= :device (get m :tag)))
(defn resource? [m node-loc] (contains?  #{:consumes :provides} (get m :tag)))

(defn node-type
  "when passed a node determine its type and return a keyword"
  [node-loc _]
  (cond
    (zip/map? node-loc)
    (let [m (zip/sexpr node-loc)]
     (cond
       (component? m node-loc) :component
       (device? m node-loc) :device
       (resource? m node-loc) :resource))))


(defprotocol RecorderProtocol
  "used by extract-transduce-store"
  (create-resource! [this urid])
  (add-fact! [this subject predicate object])
  (dump [this]))

(defmulti report node-type)

(defmethod report :default [node-loc recorder!] node-loc)

(defmethod report :component
  [comp-loc recorder!]
  (let [comp (zip/sexpr comp-loc)
        uuid (str "im:component-" (:uuid comp))
        subject (create-resource! recorder! uuid)
        frame (-> comp-loc zip/up zip/up zip/sexpr)
        frameid (symbol (str "im:frame-" (:uuid frame)))]
      (log/debug "report :component -> ")
      (add-fact! recorder! subject RDF/type (:purpose comp))
      (add-fact! recorder! subject FOAF/name (:name comp))
      (add-fact! recorder! subject RDF/subject 'im/component)
      (add-fact! recorder! subject RDF/object (:uuid comp))
      #_(add-fact! recorder! frameid RDF/node uuid)))

(defmethod report :resource
  [res-loc recorder!]
  (let [res (zip/sexpr res-loc)
        uuid (symbol (str "im/resource-" (uuid/v1)))
        subject (create-resource! recorder! uuid)
        node (-> res-loc zip/up zip/up zip/sexpr)
        nodeid (symbol (str "im/frame-" (:uuid node)))]
      (log/debug "report :resource -> ")
      (add-fact! recorder! subject RDF/type (str (:type res)))
      (add-fact! recorder! subject RDF/subject 'im/resource)
      (add-fact! recorder! subject RDF/object uuid)
      (add-fact! recorder! subject RDF/value (:value res))
      #_(add-fact! recorder! nodeid RDF/resource uuid)))

(defn extract-transduce-store
  "A script which puts the custom EDN information into RDF.
  The knowledge base (kdb) is traversed with a zipper and
  each fact is added to the facts store.
  The facts store conforms to a protocol supporting add.
  This is being implement with 'tree-seq' but if there
  are memory problems it can be refactored with a 'zipper'.
  The advantage of a zipper is that the parent nodes are
  also available.
  This could probably be done with zip/prewak."
  [recorder! xform source]
  (loop [z source]
    (when (not (zip/end? z))
      (xform z recorder!)
      (recur (zip/next z)))))

;; the use of an EDN input file removes the need for a parser
;; but if you need to load some other grammar, consider...
;;  https://github.com/aphyr/clj-antlr
;; This is probably what I will use for loading java code.
(defn data
  ([] (zip/of-file "resource/edn/deployment_instance.edn"))
  ([file-name] (zip/of-file file-name)))

(ns vuisis.immortals.json-convert
  "Convert the deployment instance into RDF updates."
  (:require
    [vuisis.immortals.convert]
    [clojure.string :as string]
    [clojure.tools.logging :as log]
    [clojure.data.json :as json]
    [clj-uuid :as uuid])
  (:import
    [org.apache.jena.sparql.vocabulary FOAF]
    [org.apache.jena.vocabulary RDF RDFS]))


(defn mock-add-fact! [s p o]
  (log/info "subject: " s ", predicate: " p ", object: " o))

(defn node-type
  "when passed a node determine its type and return a keyword"
  [node-loc _]
  (cond
    (zip/map? node-loc)
      (let [m (zip/sexpr node-loc)
            coll-type? (fn [match-set] (contains? match-set (get m :tag)))]
        (cond
          (coll-type? #{:frame}) :frame
          (coll-type? #{:component}) :component
          (coll-type? #{:device}) :device
          (coll-type? #{:artifact}) :artifact
          (coll-type? #{:environment}) :environment
          (coll-type? #{:consumes :provides}) :resource
          :else :default))))


(defn add-resource-obj
  "this function returns a zipper with a ':res'
  element added"
  [uuid-prefix zipr-loc recorder!]
  (let [zipr-obj (zip/sexpr zipr-loc)]
    (if (:res zipr-obj)
      zipr-loc ;; if the value is already set we are good
      (let [uuid (str uuid-prefix "-" (:uuid zipr-obj))
            resource (.create-resource! recorder! uuid)]
        (zip/edit zipr-loc
          #(assoc-in % [:res] resource))))))

(defmulti register
  "this multi-function registers each resource in
  the zipper by adding/updating a :res <value> to
  the indicated elements."
  node-type)

(defmethod register :default [node-loc recorder!] node-loc)

(defmethod register :frame
  [loc recorder!]
  (add-resource-obj "im:frame" loc recorder!))

(defmethod register :component
  [loc recorder!]
  (add-resource-obj "im:component" loc recorder!))

(defmethod register :artifact
  [loc recorder!]
  (add-resource-obj "im:artifact" loc recorder!))

(defmethod register :environment
  [loc recorder!]
  (add-resource-obj "im:environment" loc recorder!))

(defmethod register :device
  [loc recorder!]
  (add-resource-obj "im:device" loc recorder!))

(defmethod register :resource
  [loc recorder!]
  (add-resource-obj "im:resource" loc recorder!))


(defmulti factor
  "this multi-function extracts triple information and updates the
  recorder model"
  node-type)

(defmethod factor :default [node-loc recorder!] node-loc)

(defmethod factor :component
  [component-loc recorder!]
  (let [component (-> component-loc zip/sexpr)
        focus (:res component)
        frame (-> component-loc zip/up zip/up zip/sexpr :res)]
      (log/debug "factor :component -> " component)
      (.add-fact! recorder! focus RDF/type (:purpose component))
      (.add-fact! recorder! focus FOAF/name (:name component))
      (.add-fact! recorder! focus RDF/object (:uuid component))
      (.add-fact! recorder! frame RDFS/member focus))
  component-loc)

(defn add-factor-node
  [loc recorder! title]
  (let [record (-> loc zip/sexpr)
        focus (:res record)
        frame (-> loc zip/up zip/up zip/sexpr :res)]
      (log/debug "factor " title " -> " record)
      (.add-fact! recorder! focus RDF/type (:purpose record))
      (.add-fact! recorder! focus FOAF/name (:name record))
      (.add-fact! recorder! focus RDF/object (:uuid record))
      (.add-fact! recorder! frame RDFS/member focus))
  loc)

(defmethod factor :artifact
  [loc recorder!] (add-factor-node loc recorder! "artifact"))
(defmethod factor :environment
  [loc recorder!] (add-factor-node loc recorder! "environment"))
(defmethod factor :device
  [loc recorder!] (add-factor-node loc recorder! "device"))

(defmethod factor :resource
  [resource-loc recorder!]
  (let [resource (-> resource-loc zip/sexpr)
        focus (:res resource)
        node (-> resource-loc zip/up zip/up zip/sexpr :res)]
      (log/debug "factor :resource -> " resource)
      (.add-fact! recorder! focus RDF/type (str (:type resource)))
      (.add-fact! recorder! focus RDFS/label (:action resource))
      (.add-fact! recorder! focus RDF/object (:uuid resource))
      (.add-fact! recorder! focus RDF/value (:value resource))
      (.add-fact! recorder! node RDFS/member focus))
  resource-loc)

(defn extract-transduce-store
  "A script which puts the custom EDN information into RDF.
  The knowledge base (kdb) is traversed with a zipper and
  each fact is added to the facts store.
  The facts store conforms to a protocol supporting add.
  This is being implement with 'tree-seq' but if there
  are memory problems it can be reed with a 'zipper'.
  The advantage of a zipper is that the parent nodes are
  also available.
  This could probably be done with zip/prewak."
  [recorder! register! factor! source]
  (as->
    source $
    (loop [zipr $]
      (if (zip/end? zipr)
        zipr
        (recur (zip/next (register! zipr recorder!)))))
    (zip/edn (zip/root $)) ;; restart the zipper
    ; (do (println "registered: " (zip/sexpr $)) $)
    (loop [zipr $]
      (if (zip/end? zipr)
        zipr
        (recur (zip/next (factor! zipr recorder!)))))
    [recorder! $]))

;; the use of an EDN input file removes the need for a parser
;; but if you need to load some other grammar, consider...
;;  https://github.com/aphyr/clj-antlr
;; This is probably what I will use for loading java code.
(defn data
  ([] (zip/of-file "resource/edn/deployment_instance.edn"))
  ([file-name] (zip/of-file file-name)))

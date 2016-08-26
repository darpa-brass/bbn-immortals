
;; https://www.safaribooksonline.com/library/view/clojure-cookbook/9781449366384/
;; ... ch04.html#sec_local_io_clojure_data_to_disk
;;
(set-env!
  :source-paths #{"src/clj"}
  :resource-paths #{"resource"}
  :dependencies
  '[;[org.clojure/clojure "1.7.0"]
    ;[org.clojure/tools.reader "1.0.0-alpha3"]
    [org.slf4j/slf4j-api "1.7.14"]
    [org.clojure/tools.logging "0.3.1"]
    ; No need to specify slf4j-api, it’s required by logback
    [ch.qos.logback/logback-classic "1.1.3"]
    [me.raynes/conch "0.8.0"]
    ; [clj-beautify "0.1.3"]
    ;; working with edn as a zipper
    [rewrite-clj "0.4.12"]
    ; working with UUID
    [danlentz/clj-uuid "0.1.6"]
    ; jena rdf data store
    [org.apache.jena/apache-jena-libs "3.0.1" :extension "pom"]
    ; [org.apache.jena/apache-jena-fuseki "2.3.1" :extension "pom"]
    ])

(require '[clj-uuid :as uuid])
(require '[clojure.tools.logging :as log])
; (require '[vuisis.uml.uml :as uml] :reload)
(require '[vuisis.immortals
            [convert :as cvt]
            [example-jena :as exj]])
; (require '[vuisis.beauty :as beau] :reload)
(require '[rewrite-clj
            [parser :as parser]
            [zip :as zip]
            [reader :as reader]])
(require '[rewrite-clj.zip
             [base :as base]
             [edit :as edit]
             [seq :as seq]
             [walk :as walk]])

(deftask export
  "Build my project."
  [f file-name FILE str "the name of the file to convert"
   s store-type TYPE kw "what type of store will be written :sesame :jena :log"]
  (println "EXPORT: " file-name)
  (let [data (if file-name (cvt/data file-name) (cvt/data))]
    (case store-type
      :log (cvt/extract data cvt/report cvt/mock-add-fact!)
;      :jena (cvt/extract data cvt/report (sesame/make-add-fact! (sesame/factory)))
      (log/warn "unknown store type " store-type))))

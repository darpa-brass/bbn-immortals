
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
    ; if we want to read a json file as from webgme
    [org.clojure/data.json "0.2.6"]
    ; easier updating of nested data structure
    [com.rpl/specter "0.9.2"]
    ; working with UUID
    [danlentz/clj-uuid "0.1.6"]
    ; jena rdf data store
    [org.apache.jena/apache-jena-libs "3.0.1" :extension "pom"]
    ; objectweb https://en.wikipedia.org/wiki/ObjectWeb_ASM
    [org.ow2.asm/asm "5.0.3"]])


(require '[clj-uuid :as uuid])
(require '[clojure.tools.logging :as log])
; (require '[vuisis.uml.uml :as uml] :reload)
(require
 '[vuisis.immortals
     [edn-convert :as edn-cvt]
     [rdf-jena :as jena]])
; (require '[vuisis.beauty :as beau] :reload)
(require
 '[rewrite-clj
     [parser :as parser]
     [zip :as zip]
     [reader :as reader]])
(require
 '[rewrite-clj.zip
     [base :as base]
     [edit :as edit]
     [seq :as seq]
     [walk :as walk]])
(require '[com.rpl.specter :as specter])
(require '[clojure.inspector :as inspector])

(defn- export-impl
  "Build my project."
  [file-name source-type target-type]
  (try
    (let [data (if file-name
                 (edn-cvt/data file-name)
                 (edn-cvt/data))]
      (case [source-type target-type]
        ; :log (edn-cvt/extract data edn-cvt/report edn-cvt/mock-add-fact!)
        ; :kr-sesame (edn-cvt/extract data edn-cvt/report (sesame/make-add-fact! (sesame/factory)))
        ; :kr-jena (edn-cvt/extract data edn-cvt/report (kr/make-add-fact! (kr/factory)))
        ; :seabass (edn-cvt/extract data edn-cvt/report (seabass/make-add-fact! (seabass/factory)))
        [:edn :ttl
          (let [record! (jena/make-model)
                [model zipper]
                (edn-cvt/extract-transduce-store
                  record! edn-cvt/register edn-cvt/factor data)]
            (.dump model))]
        (log/warn "unknown conversion type(s): [" source-type " " target-type "]"))
      (catch Exception ex
        (log/error "top catch: " ex)))))

(deftask export
  "Build my project."
  [f file-name FILE str "the name of the file to convert"
   s source-type TYPE kw "what type of store will be read :json :edn"
   t target-type TYPE kw "what type of store will be written :sesame :ttl :log"]
  ((fnil println nil "<no file name given>") "EXPORT: " file-name )
  (export-impl file-name source-type target-type))

(deftask show-sample
  [f file-name FILE str "the name of the file to convert"]
  (println "SHOW: " file-name)
  (try
    (let [data (if file-name (edn-cvt/data file-name) (edn-cvt/data))]
      (inspector/inspect-tree data))))

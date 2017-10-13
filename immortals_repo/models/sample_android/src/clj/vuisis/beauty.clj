
(ns vuisis.beauty
  (:require [clj-beautify.core :as b]))

(defn clj [path]
  (b/format-file path "clj"))

(defn edn [path]
  (b/format-file path "edn"))

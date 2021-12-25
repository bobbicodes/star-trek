(ns trek
  (:require [cheshire.core :as json]
            [clojure.string :as str]))

(defn episode [series ep]
  (get-in (json/parse-string (slurp "resources/all_scripts_raw.json"))
          [series ep]))

(defn script-seq 
  "Returns a seq of alternating characters and their lines."
  [series ep]
  (interleave
   (conj (re-seq #"[A-Z]+:" (episode series ep)) nil)
   (str/split (episode series ep) #"[A-Z]+:")))

(script-seq "TOS" "episode 0")

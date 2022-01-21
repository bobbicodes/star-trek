(ns trek
  (:require [cheshire.core :as json]
            [clojure.string :as str]))

(defn fetch-ep [s e]
  (slurp (str "https://www.springfieldspringfield.co.uk/view_episode_scripts.php?tv-show=star-trek-discovery-2017&episode=s" s "e" e)))

(defn picard [s e]
  (slurp (str "https://www.springfieldspringfield.co.uk/view_episode_scripts.php?tv-show=star-trek-picard-2020&episode=s" s "e" e)))

(comment
  (doseq [s ["01"]
          e ["01" "02" "03" "04" "05" "06" "07" "08" "09" "10" "11" "12" "13" "14" "15"]]
    (spit (str "resources/picard/picard-s" s "e" e) (picard s e)))
  )

(defn disco-seq 
  "Extracts the <div> containing the episode script
   and outputs a sequence of lines."
  [s e]
  (->  (slurp (str "resources/disco/disco-s" s "e" e))
       (str/split #"<div class=\"scrolling-script-container\">")
       last
       (str/split #"</div>")
       first
       (str/split #"<br>")))

(comment
  (doseq [line  (disco-seq "04" "07")]
    (prn line))
  )

(defn episode [series ep]
  (get-in (json/parse-string (slurp "resources/all_scripts_raw.json"))
          [series ep]))

(defn script-seq 
  "Returns a seq of alternating characters and their lines."
  [series ep]
  (interleave
   (conj (re-seq #"[A-Z]+:" (episode series ep)) nil)
   (str/split (episode series ep) #"[A-Z]+:")))

(comment
  (script-seq "TOS" "episode 0")
  )

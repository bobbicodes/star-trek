(ns trek
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [babashka.pods :as pods]
            [babashka.curl :as curl]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")
(require '[pod.retrogradeorbit.bootleg.utils :refer [convert-to]]
         '[pod.retrogradeorbit.hickory.select :as s]
         '[pod.retrogradeorbit.bootleg.enlive :as enlive])

(defn fetch-url [url] (:body (curl/get url {:compressed false})))

(defn fetch-disco [s e]
  (fetch-url (str "https://www.springfieldspringfield.co.uk/view_episode_scripts.php?tv-show=star-trek-discovery-2017&episode=s" s "e" e)))

(defn fetch-picard [s e]
  (fetch-url (str "https://www.springfieldspringfield.co.uk/view_episode_scripts.php?tv-show=star-trek-picard-2020&episode=s" s "e" e)))

(defn fetch-snw [s e]
  (let [base-url "https://transcripts.foreverdreaming.org/viewtopic.php?t="
        posts
        {:s01e01 "72376"
         :s01e02 "72959"
         :s01e03 "73149"
         :s01e04 "73773"
         :s01e05 "73827"
         :s01e06 "74201"
         :s01e07 "74648"
         :s01e08 "75383"
         :s01e09 "75964"
         :s01e10 "76357"
         :s02e01 "116179"
         :s02e02 "116968"
         :s02e03 "118367"
         :s02e04 "119628"
         :s02e05 "120667"}
        k (keyword (str "s" s "e" e))]
  (str base-url (k posts))
    #_(fetch-url (str base-url (k posts)))))

(fetch-snw "01" "01")

(comment
  ;; TODO: parse xml
  ;; try https://github.com/retrogradeorbit/bootleg
  ;; see "Extract HTML tables with babashka and bootleg":
  ;; ""https://gist.github.com/borkdude/fc64444a4e7aea4eb647ce42888d1adf
  ;; For SNW, search for tag <div class=\"content\">
  (convert-to (fetch-url "https://transcripts.foreverdreaming.org/viewtopic.php?t=72376") :hiccup)
  )

(def clojure-html (:body (curl/get "https://en.wikipedia.org/wiki/Clojure"  {:compressed false})))

(def hiccup (convert-to (fetch-url "https://transcripts.foreverdreaming.org/viewtopic.php?t=72376") :hiccup))
(def tables (atom []))
(def divs (atom []))

(enlive/at hiccup [:table] ;; [:table] is a "css like" selector
           #(do
              ;; function will be called with hickory forms.
              ;; so convert them to hiccup as we conj
              (swap! tables conj (convert-to % :hiccup))

              ;; enlive expect a transformed form to be returned.
              ;; just return the same to transform nothing
              %))

(enlive/at hiccup [:div] ;; [:div] is a "css like" selector
           #(do
              ;; function will be called with hickory forms.
              ;; so convert them to hiccup as we conj
              (swap! divs conj (convert-to % :hiccup))

              ;; enlive expect a transformed form to be returned.
              ;; just return the same to transform nothing
              %))

@tables

(def hiccup-snw (convert-to (fetch-snw "01" "01") :hiccup))
(def hickory (convert-to (fetch-snw "01" "01") :hickory))
;; select all div tags from markup.
;; will return a vector of hickory structures
(def divs-hickory (s/select (s/tag :div) hickory))

divs-hickory

(defn episode-seq 
  "Extracts the <div> containing the episode script
   and outputs a sequence of lines."
  [html]
  (->  html
     (str/split #"<div class=\"scrolling-script-container\">")
     last
     (str/split #"</div>")
     first
     (str/split #"<br>")))

(comment
  (episode-seq (fetch-disco "01" "02"))
  (episode-seq (fetch-picard "01" "02"))
  )

(defn local-episode [series ep]
  (get-in (json/parse-string (slurp "resources/all_scripts_raw.json"))
          [series ep]))

(defn local-seq 
  "Returns a seq of alternating characters and their lines."
  [series ep]
  (interleave
   (conj (re-seq #"[A-Z]+:" (local-episode series ep)) nil)
   (str/split (local-episode series ep) #"[A-Z]+:")))

(comment
  (local-seq "TOS" "episode 0")
  )

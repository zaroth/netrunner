(ns netrunner.admin
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as sab :include-macros true]
            [netrunner.ajax :refer [GET]]))
(def editors ["mtgred" "nealterrell" "Saintis" "zaroth" "JoelCFC25" "queueseven"
              "mediohxcore" "d1en's cat" "Apex" "Azogar"])

(defn- runner-winrates []
  [["Whizzard: Master Gamer" (+ 80 (rand 10))]
   ["MaxX: Maximum Punk Rock" (+ 75 (rand 20))]
   ["Valencia Estevez: The Angel of Cayambe" (+ 70 (rand 5))]
   ["Noise: Hacker Extraordinaire" (+ 68 (rand 23))]
   ["The Professor: Keeper of Knowledge" (+ 50 (rand 50))]])

(defn- corp-winrates []
  [["Haas-Bioroid: Engineering the Future" (+ 60 (rand 10))]
   ["Cerebral Imaging: Infinite Frontiers" 7]])

(defn- rng-settings []
  {:capap (rand-int 6)
   :snipe (zero? (rand-int 2))
   :siphon (zero? (rand-int 2))})

(defn- connect-settings []
  {:disconnect (zero? (rand-int 2))
   :blame (zero? (rand-int 2))
   :gg (zero? (rand-int 2))
   :concedes (zero? (rand-int 2))
   :igdecks (zero? (rand-int 2))})

(defn- community-stats []
  {
   :octgn (+ 5 (rand-int 30))
   :nrdb (+ 100 (rand-int 200))
   :skill (+ 50 (rand-int 30))
   })

(defn update-community-stats [owner]
  (om/set-state! owner [:community-stats :octgn] (+ (rand-int 2) (om/get-state owner [:community-stats :octgn])))
  (om/set-state! owner [:community-stats :nrdb] (+ (rand-int 10) (om/get-state owner [:community-stats :nrdb])))
  (om/set-state! owner [:community-stats :skill] (+ (rand-int 5) (om/get-state owner [:community-stats :skill]))))

(defn- new-state []
  {:runner-winrates (runner-winrates)
   :corp-winrates (corp-winrates)
   :rng (rng-settings)
   :connect (connect-settings)
   :editor (first (shuffle editors))
   :community-stats (community-stats)
   :spamming {:increment 0 :text [:p ""]}})

(defn increment-spam [owner](om/set-state! owner [:spamming :text] "Assembling bad cards...")
  (om/set-state! owner [:spamming :increment] (inc (om/get-state owner [:spamming :increment])))
  (om/set-state! owner [:spamming :text] (let [i (om/get-state owner [:spamming :increment])]
                                           (list
                                           [:p "Assembling bad cards... " (when (< 0 i) "Done.")]
                                           (when (< 1 i) [:p "Writing a novel for description... " (when (< 2 i) "Done.")])
                                           (when (< 2 i) [:p "Bribing Alsciende... " (when (< 3 i) "Done.")])
                                           (when (< 3 i) [:p "Spamming likes... " (when (< 4 i) "Done.")])
                                           (when (< 4 i) [:p "Congratulations, '" (om/get-state owner :decklist-link) "' is the new deck of the week on NRDB!"])))))

(def app-state (atom (new-state)))

(defn admin [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (js/setInterval
        (fn [] (om/set-state! owner :editor (first (shuffle editors))))
        60000)
      (js/setInterval
        (fn []
          (om/set-state! owner :corp-winrates (corp-winrates))
          (om/set-state! owner :runner-winrates (runner-winrates))
          (om/set-state! owner :rng (rng-settings))
          (update-community-stats owner)
          (om/set-state! owner :connect (connect-settings)))
        5000))
    om/IInitState
    (init-state [this]
      (new-state))

    om/IRenderState
    (render-state [this state]
      (sab/html
      [:div.admin.panel.blue-shade
       [:h1 "Admin panel"]

       [:p "Currently being controlled by: " [:b {:style {:color "red"}} (om/get-state owner :editor)]]

       [:h2 "RNG settings"]
       [:p "CapAp's corp starting hand agenda density: "
        (do (for [option [0 1 2 3 4 5]]
          [:label [:input {:type "radio"
                           :style {:margin-left "15px"}
                           :name "rng"
                           :value option
                           :checked (= (om/get-state owner [:rng :capap]) option)}]
           option]))]
       [:p [:input {:disabled true :type "checkbox" :checked (om/get-state owner [:rng :snipe])}
            "Snipe only agenda during HQ single access"]]
       [:p [:input {:disabled true :type "checkbox" :checked (om/get-state owner [:rng :siphon])}
            "Force Account Siphon in runner's opening hand"]]

       [:h2 "Player behavior manipulation"]
       [:p [:input {:disabled true :type "checkbox" :checked (om/get-state owner [:connect :disconnect])}
            "Fake disconnect when losing"]]
       [:p [:input {:disabled true :type "checkbox" :checked (om/get-state owner [:connect :blame])}
            "Blame terrible Internet connection on Weyland.meat servers"]]
       [:p [:input {:disabled true :type "checkbox" :checked (om/get-state owner [:connect :gg])}
            "Hide 'gg' typed by players"]]
       [:p [:input {:disabled true :type "checkbox" :checked (om/get-state owner [:connect :concedes])}
            "Show concedes as ragequits"]]
       [:p [:input {:disabled true :type "checkbox" :checked (om/get-state owner [:connect :igdecks])}
            "Add AI bots to lobby playing museum IG decks"]]

       [:h2 "Latest decklists"]
       [:h4 "Dan D'Argenio"]
       [:ol
        [:li [:i "For mtgred's eyes only."]]]

       [:h4 "Chill84"]
       [:ol
        [:li "2x Account Siphon"]
        [:li "1x Apocalypse"]
        [:li "1x Yog.0"]
        [:li "1x Levy AR Lab Access"]
        [:li "43x Orange cards selected by your cat"]]

       [:h4 "Calimsha"]
       [:ul
        [:li "3x Diesel"]
        [:li "3x Dirty Laundry"]
        [:li "1x Indexing"]
        [:li "1x Legwork ••"]
        [:li "1x Levy AR Lab Access"]
        [:li "3x Lucky Find ••••• •"]
        [:li "2x Quality Time"]
        [:li "1x Scavenge"]
        [:li "1x Stimhack •"]
        [:li "3x Sure Gamble"]
        [:li "3x The Maker's Eye"]
        [:li "2x Astrolabe"]
        [:li "3x Clone Chip ☆☆☆"]
        [:li "1x Plascrete Carapace"]
        [:li "3x Prepaid VoicePAD ☆☆☆"]
        [:li "1x Same Old Thing"]
        [:li "1x Symmetrical Visage"]
        [:li "1x Atman"]
        [:li "2x Cerberus \"Lady\" H1 ☆☆"]
        [:li "1x Cyber-Cypher"]
        [:li "1x Mimic •"]
        [:li "1x Sharpshooter"]
        [:li "1x ZU.13 Key Master"]
        [:li "1x Clot ••"]
        [:li "1x Datasucker •"]
        [:li "1x Parasite ☆ ••"]
        [:li "3x Self-modifying Code"]
        [:li "What do you mean, this isn't legal anymore?"]]

       [:h2 "NRDB Deck of the Week spambot"]
       [:p "Enter deck name: " [:input {:type "text" :style {:width "500px"}
                                            :value (om/get-state owner :decklist-link)
                                            :on-change #(om/set-state! owner :decklist-link (.. % -target -value))}]]
       [:p [:button {:type "button" :on-click #((om/set-state! owner [:spamming :text] [:p "Assembling bad cards..."])
                                                (om/set-state! owner [:spamming :increment] 0)
                                                (js/setInterval
                                                  (fn [] (increment-spam owner))
                                                  3000))}
            "Promote a deck"]
        (om/get-state owner [:spamming :text])]

       [:h2 "Community statistics"]
       [:p "People wanting to just go back to OCTGN: " (om/get-state owner [:community-stats :octgn])]
       [:p "Complaints about NRDB deck of the week: " (om/get-state owner [:community-stats :nrdb])]
       [:p "Complaints about low skill level on weyland.meat: " (om/get-state owner [:community-stats :skill])]

       [:h2 "Winrate reports"]
       [:h3 "Runner"]
       [:ol
        (for [run (sort-by second > (om/get-state owner :runner-winrates))]
          [:li (str (first run) " - " (second run) "%")])]
       [:h3 "Corp"]
       [:ol
        (for [run (sort-by second > (om/get-state owner :corp-winrates))]
          [:li (str (first run) " - " (second run) "%")])]

       [:h2 "Run Last Click safety word"]
       [:p [:a {:href "https://www.youtube.com/watch?v=-FaoCXTSUQc"} "PERSEPHONE"]]]))))

(om/root admin app-state {:target (. js/document (getElementById "admin"))})
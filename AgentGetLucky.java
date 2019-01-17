import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.EndNegotiation;
import negotiator.actions.Offer;
import negotiator.issue.*;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

import java.util.*;

/**
 * Like the legend of the phoenix
 * All ends with beginnings
 * What keeps the planet spinning (uh)
 * The force of love beginning
 * We've come too far to give up who we are
 * So let's raise the bar and our cups to the stars...
 */
public class AgentGetLucky extends AbstractNegotiationParty {
    private final String description = "AgentGetLucky";

    private Bid lastReceivedBid; // offer on the table
    private Bid myLastBid;
    private AdditiveUtilitySpace space;
    private List<Issue> issues;
    private Set<Map.Entry<Objective, Evaluator>> evaluators;
    private OpponentModel op1 = new OpponentModel();
    private OpponentModel op2 = new OpponentModel();
    private Preference pref = new Preference();
    private int roundCount = 0;
    private Double[] omegas = new Double[] {0.0, 0.25, 0.50, 0.75, 1.00};

    /**
     * A class for storing our agent's value preference in issues
     */
    private class Preference {
        public HashMap<String, IssueWeight> weights = new HashMap<>();

        public void addIssue(IssueDiscrete issue, Double weighting) {
            IssueWeight iw = weights.getOrDefault(issue.getName(), new IssueWeight(weighting));
            weights.put(issue.getName(), iw);
        }

        public class IssueWeight {
            public Double weighting;
            public Integer maxValue = Integer.MIN_VALUE;
            private HashMap<String, Integer> evals = new HashMap<>();

            public IssueWeight(Double weighting) {
                this.weighting = weighting;
            }

            public void putWeight(ValueDiscrete value, Integer weight ) {
                this.evals.put(value.getValue(), weight);
                if(weight > maxValue) maxValue = weight;
            }

            public Integer getWeight(ValueDiscrete value) {
                return this.evals.get(value.getValue());
            }

            public Double getDisUtility(ValueDiscrete value) {
                Integer weight = this.getWeight(value);
                return weighting * (weight - this.maxValue) / this.maxValue;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, IssueWeight> entry: this.weights.entrySet()) {
                sb.append("\t" + entry.getKey() + ": " + entry.getValue().evals + "\n");
            }
            return sb.toString();
        }
    }

    // https://github.com/tdgunes/ExampleAgent/wiki/Accessing-the-evaluation-of-a-value
    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
        this.space = (AdditiveUtilitySpace) this.getUtilitySpace();
        this.issues = this.space.getDomain().getIssues();
        this.evaluators = this.space.getEvaluators();

        // Init issues and values in opponent's model
        for(Issue issue: this.issues) {
            int issueNumber = issue.getNumber();

            // Assuming that issues are discrete only
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) this.space.getEvaluator(issueNumber);

            op1.addIssue(issueDiscrete);
            op2.addIssue(issueDiscrete);
            pref.addIssue(issueDiscrete, this.space.getWeight(issueNumber));

            OpponentModel.IssueStore is1 = op1.getIssue(issueDiscrete);
            OpponentModel.IssueStore is2 = op2.getIssue(issueDiscrete);

//            System.out.println("IssueName " + issueDiscrete.getName());

            for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {

//                System.out.println("ValueName " + valueDiscrete.getValue());
//                System.out.println("Evaluation(getValue): " + evaluatorDiscrete.getValue(valueDiscrete));

//                try {
//                    System.out.println("Evaluation(getEvaluation): " + evaluatorDiscrete.getEvaluation(valueDiscrete));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                is1.addValue(valueDiscrete);
                is2.addValue(valueDiscrete);

                try {
                    Integer vv = evaluatorDiscrete.getValue(valueDiscrete);
                    pref.weights
                        .get(issueDiscrete.getName())
                        .putWeight(valueDiscrete, vv);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            System.out.println(pref.toString());
        }
    }

    /**
     * When this function is called, it is expected that the Party chooses one of the actions from the possible
     * action list and returns an instance of the chosen action.
     *
     * @param list
     * @return
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {

        // Keep track of number of rounds
        ++this.roundCount;

        // Get current time (ranges from 0 to 1)
        double time = getTimeLine().getTime();

        // End Negotiation if end of time
        if (time >= 1.0D) return new EndNegotiation(this.getPartyId());

        // Check if last received offer is higher than current offer
        if (lastReceivedBid != null &&
                myLastBid != null &&
                this.hasHigherUtility(lastReceivedBid, myLastBid)) {
            return new Accept(this.getPartyId(), lastReceivedBid);
        }

        // Return Offer with highest utility for the first half of negotiation
        if(time < 0.5D) {
            Offer max = new Offer(this.getPartyId(), this.getMaxUtilityBid());
            this.myLastBid = max.getBid();
            return max;
        }

        // Accept anything higher than our threshold limit in the 2nd half of negotiation
        if(this.utilitySpace.getUtility(lastReceivedBid) > this.getThresholdLimit(time)) {
            return new Accept(this.getPartyId(), lastReceivedBid);
        }

        // Make a new offer if opponent's offer is lower than out threshold
        Offer newOffer = new Offer(this.getPartyId(), this.getBid());
        this.myLastBid = newOffer.getBid();
        return newOffer;
    }

    /**
     * This method is called to inform the party that another NegotiationParty chose an Action.
     * @param sender
     * @param action
     */
    @Override
    public void receiveMessage(AgentID sender, Action action) {
        super.receiveMessage(sender, action);

        if (sender != null && (action instanceof Offer || action instanceof Accept)) {

            OpponentModel op = this.getOpponentByName(sender.getName());

            if (action instanceof Offer) { // sender is making an offer

                Offer offer = (Offer) action;

                Bid currentBid = offer.getBid();

                lastReceivedBid = currentBid;

                for (Issue issue: currentBid.getIssues()) {
                    IssueDiscrete isd = (IssueDiscrete) issue;
                    ValueDiscrete vd = (ValueDiscrete) currentBid.getValue(issue.getNumber());
                    op.getIssue(isd).addCount(vd);
                }
            }
        }
    }

    private Bid getBid() {
        Double time = this.timeline.getTime();
        Double cdu = 0D; // cumulative dis-utility
        List<Issue> shuffle = new ArrayList<>(this.issues);
        java.util.Collections.shuffle(shuffle);

        // For storing issue number and Calue classes for each issue
        HashMap<Integer, Value> bidStore = new HashMap<>();

        for(Issue issue: shuffle) {
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            ArrayList<ValueDiscrete> valueStores = new ArrayList<>();
            ArrayList<Double> combinedOpponentWeights = new ArrayList<>();
            Double sumoo = 0D;
            Double disUtilityThreshold = this.getThresholdLimit(time) - 1;

            // keep track highest dis-utility if no value is above disUtilityThreshold
            Double highestDis = Double.MIN_VALUE;
            ValueDiscrete highestDisValue = issueDiscrete.getValues().get(0); // Initialize a default highestDisValue value

            for(ValueDiscrete valueDiscrete: issueDiscrete.getValues()) {

                Double dis = this.pref.weights.get(issueDiscrete.getName()).getDisUtility(valueDiscrete);

                // keep tracks of value with highest dis-Utility
                if(dis > highestDis) {
                    highestDis = dis;
                    highestDisValue = valueDiscrete;
                }

                // if cdu + dis is below disUtilityThreshold...unsure, need to check with David
                if(cdu + dis < disUtilityThreshold) continue;

                Double combined = getCombinedOpponentModelWeight(issueDiscrete, valueDiscrete);
                combinedOpponentWeights.add(combined);
                valueStores.add(valueDiscrete);

                sumoo += combined;
            }

            if(valueStores.size() == 0) valueStores.add(highestDisValue);

            Integer idx = 0;
            Double rand = Math.random();
            for(int i = 0; i < combinedOpponentWeights.size(); i++) {
                if(rand < combinedOpponentWeights.get(i)/sumoo) {
                    idx = i;
                    break;
                }
            }

            ValueDiscrete picked = valueStores.get(idx);

            bidStore.put(issueDiscrete.getNumber(), picked);

            cdu += this.pref.weights.get(issueDiscrete.getName()).getDisUtility(picked);
        }

        return new Bid(this.space.getDomain(), bidStore);
    }

    private Double getCombinedOpponentModelWeight(IssueDiscrete issue, ValueDiscrete value) {
        Integer oo1 = op1.getIssue(issue).getValueCount(value);
        Integer oo2 = op2.getIssue(issue).getValueCount(value);
        Double omega = this.getOmega(true); // change this to false for equal weight i.e. 0.5
        return omega * oo1 + (1-omega) * oo2;
    }

    private Double getThresholdLimit(Double time) {
//        if(time > 1.0D) return 0.8;
//        else return 1.0D - (0.2 * time);

        // Issue 3 run 4.2 code - taken from ParsCat
        double util = 1.0D;
        if (time <= 0.25D) util = 1.0D - time * 0.4D;
        if (time > 0.25D && time <= 0.375D) util = 0.9D + (time - 0.25D) * 0.4D;
        if (time > 0.375D && time <= 0.5D) util = 0.95D - (time - 0.375D) * 0.4D;
        if (time > 0.5D && time <= 0.6D) util = 0.9D - (time - 0.5D);
        if (time > 0.6D && time <= 0.7D) util = 0.8D + (time - 0.6D) * 2.0D;
        if (time > 0.7D && time <= 0.8D) util = 1.0D - (time - 0.7D) * 3.0D;
        if (time > 0.8D && time <= 0.9D) util = 0.7D + (time - 0.8D) * 1.0D;
        if (time > 0.9D && time <= 0.95D) util = 0.8D - (time - 0.9D) * 6.0D;
        if (time > 0.95D) util = 0.5D + (time - 0.95D) * 4.0D;
        if (time > 1.0D) util = 0.7D;
        return util;
    }

    private Double getOmega(Boolean isRandom) {
        int pos = getRandomNumberInRange(0, omegas.length-1);
        return isRandom ? omegas[pos] : 0.5;
    }

    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private OpponentModel getOpponentByName(String name) {

        // check names are set for opponents
        if(op1.getName() == null) {
            op1.setName(name);
        } else if(op2.getName() == null) {
            op2.setName(name);
        }

        return op1.getName().equals(name) ? op1 : op2;
    }

    private Bid getMaxUtilityBid() {
        try {
            return this.utilitySpace.getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Boolean hasHigherUtility(Bid firstOffer, Bid secondOffer) {
        return this.utilitySpace.getUtility(firstOffer) >= this.utilitySpace.getUtility(secondOffer);
    }

    /**
     * A human-readable description for this party.
     * @return
     */
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    protected void finalize() throws Throwable {
//        System.out.println(op1.toString());
//        System.out.println(op2.toString());
//        System.out.println(pref.toString());
        super.finalize();
    }
}
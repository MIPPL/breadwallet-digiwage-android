package com.wagerrwallet.presenter.entities;


/**
 * BreadWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 1/13/16.
 * Copyright (c) 2016 breadwallet LLC
 * <p>
 *
 * (c) Wagerr Betting platform 2019
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class BetEventEntity {
    public static final String TAG = BetEventEntity.class.getName();

    public enum BetTxType {
        PEERLESS(0x02),
        UPDATEODDS(0x05),
        CHAIN_LOTTO(0x06),
        PEERLESS_SPREAD(0x09),
        PEERLESS_TOTAL(0x0a),
        UNKNOWN(-1);

        private int type;
        BetTxType(int type) {
            this.type = type;
        }

        public int getNumber()    {return type;}

        public static BetTxType fromValue (int value) {
            // Just a linear search - easy, quick-enough.
            for (BetTxType eventType : BetTxType.values())
                if (eventType.type == value)
                    return eventType;
            return UNKNOWN;
        }
    }

    // table data
    protected long blockheight;
    protected long timestamp;
    protected String txHash;
    protected String txISO;
    protected long version;
    protected BetTxType type;
    protected long eventID;
    protected long eventTimestamp;
    protected long sportID;
    protected long tournamentID;
    protected long roundID;
    protected long homeTeamID;
    protected long awayTeamID;
    protected long homeOdds;
    protected long awayOdds;
    protected long drawOdds;
    protected long entryPrice;
    protected long spreadPoints;
    protected long totalPoints;
    protected long overOdds;
    protected long underOdds;

    // mappings
    protected String txSport;
    protected String txTournament;
    protected String txRound;
    protected String txHomeTeam;
    protected String txAwayTeam;

    // results
    protected BetResultEntity.BetResultType resultType;
    protected long homeScore;
    protected long awayScore;

    // constructor for DB
    public BetEventEntity(String txHash, BetTxType type, long version,
                          long eventID, long eventTimestamp, long sportID, long tournamentID, long roundID,
                          long homeTeamID, long awayTeamID, long homeOdds, long awayOdds, long drawOdds,
                          long entryPrice, long spreadPoints, long totalPoints, long overOdds, long underOdds,
                          long blockheight, long timestamp, String iso) {
        this.blockheight = blockheight;
        this.timestamp = timestamp;
        this.txHash = txHash;
        this.txISO = iso;

        this.version = version;
        this.type = type;
        this.eventID = eventID;
        this.sportID = sportID;
        this.roundID = roundID;
        this.tournamentID = tournamentID;

        this.eventTimestamp = eventTimestamp;
        this.homeTeamID = homeTeamID;
        this.awayTeamID = awayTeamID;
        this.homeOdds = homeOdds;
        this.awayOdds = awayOdds;
        this.drawOdds = drawOdds;
        this.entryPrice = entryPrice;
        this.spreadPoints = spreadPoints;
        this.totalPoints = totalPoints;
        this.overOdds = overOdds;
        this.underOdds = underOdds;
    }

    // extended constructor with support text for UI
    public BetEventEntity(String txHash, BetTxType type, long version,
                          long eventID, long eventTimestamp, long sportID, long tournamentID, long roundID,
                          long homeTeamID, long awayTeamID, long homeOdds, long awayOdds, long drawOdds,
                          long entryPrice, long spreadPoints, long totalPoints, long overOdds, long underOdds,
                          long blockheight, long timestamp, String iso,
                          String txSport, String txTournament, String txRound, String txHomeTeam, String txAwayTeam,    // mappings
                          BetResultEntity.BetResultType resultType, long homeScore, long awayScore) {

        this( txHash, type, version, eventID, eventTimestamp, sportID, tournamentID, roundID,
                homeTeamID,  awayTeamID, homeOdds, awayOdds, drawOdds,
                entryPrice,  spreadPoints, totalPoints, overOdds, underOdds,
                blockheight, timestamp, iso );

        this.txSport = txSport;
        this.txTournament = txTournament;
        this.txRound = txRound;
        this.txHomeTeam = txHomeTeam;
        this.txAwayTeam = txAwayTeam;

        this.resultType = resultType;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    protected BetEventEntity() {
    }

    public long getBlockheight() {
        return blockheight;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTxHash() {
        return txHash;
    }

    public String getTxISO() {
        return txISO;
    }

    public long getVersion() {
        return version;
    }

    public BetTxType getType() {
        return type;
    }

    public long getEventID() {
        return eventID;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public long getSportID() {
        return sportID;
    }

    public long getTournamentID() {
        return tournamentID;
    }

    public long getRoundID() {
        return roundID;
    }

    public long getHomeTeamID() {
        return homeTeamID;
    }

    public long getAwayTeamID() {
        return awayTeamID;
    }

    public long getHomeOdds() {
        return homeOdds;
    }

    public long getAwayOdds() {
        return awayOdds;
    }

    public long getDrawOdds() {
        return drawOdds;
    }

    public long getEntryPrice() {
        return entryPrice;
    }

    public long getSpreadPoints() {
        return spreadPoints;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public long getOverOdds() {
        return overOdds;
    }

    public long getUnderOdds() {
        return underOdds;
    }

    public String getTxSport() {
        return txSport;
    }

    public String getTxTournament() {
        return txTournament;
    }

    public String getTxRound() {
        return txRound;
    }

    public String getTxHomeTeam() {
        return txHomeTeam;
    }

    public String getTxAwayTeam() {
        return txAwayTeam;
    }
}

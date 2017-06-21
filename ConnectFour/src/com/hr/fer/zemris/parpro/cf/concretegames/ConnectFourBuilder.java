/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hr.fer.zemris.parpro.cf.concretegames;


public class ConnectFourBuilder {

    private String firstPlayer = "Alice";
    private String secondPlayer = "Bob";
    private int rowCnt = 6;
    private int colCnt = 7;

    public ConnectFourBuilder() {
    }

    public ConnectFourBuilder setFirstPlayer(String firstPlayer) {
        this.firstPlayer = firstPlayer;
        return this;
    }

    public ConnectFourBuilder setSecondPlayer(String secondPlayer) {
        this.secondPlayer = secondPlayer;
        return this;
    }

    public ConnectFourBuilder setRowCnt(int rowCnt) {
        this.rowCnt = rowCnt;
        return this;
    }

    public ConnectFourBuilder setColCnt(int colCnt) {
        this.colCnt = colCnt;
        return this;
    }

    public ConnectFour createConnectFour() {
        return new ConnectFour(firstPlayer, secondPlayer, rowCnt, colCnt);
    }
    
}

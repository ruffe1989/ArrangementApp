package no.usn.grupp1.arrangementapp;

public class Billett {

    private String tittel;
    private int seat;
    private int fee;
    private int ticket;

    Billett(String tittel, int seat, int fee, int ticket){
        this.tittel = tittel;
        this.seat = seat;
        this.fee = fee;
        this.ticket = ticket;
    }

    public String getTittel() {
        return tittel;
    }

    public int getSeat() {
        return seat;
    }

    public int getFee() {
        return fee;
    }

    public int getTicket() { return ticket; }
}

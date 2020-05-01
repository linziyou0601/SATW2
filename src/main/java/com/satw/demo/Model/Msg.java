package com.satw.demo.Model;

public class Msg {
    public String title = "";
    public String text = "";
    public String icon = "success";
    public String confirmButtonText = "Ok";
    public String cancelButtonText = "Cancel";
    public String confirmButtonColor = "#3085d6";
    public String cancelButtonColor = "#aaaaaa";
    public boolean showConfirmButton = true;
    public boolean showCloseButton = false;
    public boolean allowEscapeKey = true;
    public boolean allowOutsideClick = true;
    public Msg(){};
    public Msg(String title, String text, String icon){
        this.title = title;
        this.text = text;
        this.icon = icon;
    }
}
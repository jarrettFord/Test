/*  1:   */ package com.klintos.apocalypsebot.utils;
/*  2:   */ 
/*  3:   */ import java.io.BufferedReader;
/*  4:   */ import java.io.File;
/*  5:   */ import java.io.FileReader;
/*  6:   */ import java.util.ArrayList;
/*  7:   */ 
/*  8:   */ public class FileUtils
/*  9:   */ {
/* 10:   */   public static void loadFile(String fileLocation, ArrayList array)
/* 11:   */   {
/* 12:   */     try
/* 13:   */     {
/* 14:15 */       File file = new File(fileLocation);
/* 15:   */       
/* 16:17 */       BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
/* 17:19 */       for (String readString = ""; (readString = bufferedReader.readLine()) != null;) {
/* 18:20 */         array.add(readString);
/* 19:   */       }
/* 20:   */     }
/* 21:   */     catch (Exception e)
/* 22:   */     {
/* 23:23 */       e.printStackTrace();
/* 24:   */     }
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.klintos.apocalypsebot.utils.FileUtils
 * JD-Core Version:    0.7.0.1
 */
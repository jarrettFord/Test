/*   1:    */ 
/*   2:    */ 
/*   3:    */ com.klintos.apocalypsebot.utils.Bot
/*   4:    */ com.klintos.apocalypsebot.utils.FileUtils
/*   5:    */ java.awt.Adjustable
/*   6:    */ java.awt.Container
/*   7:    */ java.awt.Font
/*   8:    */ java.awt.event.ActionEvent
/*   9:    */ java.awt.event.ActionListener
/*  10:    */ java.awt.event.AdjustmentEvent
/*  11:    */ java.awt.event.AdjustmentListener
/*  12:    */ java.awt.event.KeyEvent
/*  13:    */ java.awt.event.KeyListener
/*  14:    */ java.io.File
/*  15:    */ java.util.ArrayList
/*  16:    */ javax.swing.DefaultListModel
/*  17:    */ javax.swing.JButton
/*  18:    */ javax.swing.JCheckBox
/*  19:    */ javax.swing.JFileChooser
/*  20:    */ javax.swing.JFrame
/*  21:    */ javax.swing.JLabel
/*  22:    */ javax.swing.JList
/*  23:    */ javax.swing.JScrollBar
/*  24:    */ javax.swing.JScrollPane
/*  25:    */ javax.swing.JSlider
/*  26:    */ javax.swing.JTextField
/*  27:    */ javax.swing.UIManager
/*  28:    */ javax.swing.UnsupportedLookAndFeelException
/*  29:    */ javax.swing.event.ChangeEvent
/*  30:    */ javax.swing.event.ChangeListener
/*  31:    */ org.spacehq.mc.auth.exception.AuthenticationException
/*  32:    */ org.spacehq.mc.protocol.ProtocolConstants
/*  33:    */ org.spacehq.mc.protocol.data.message.Message
/*  34:    */ org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket
/*  35:    */ org.spacehq.mc.protocol.packet.ingame.server.ServerChatPacket
/*  36:    */ org.spacehq.packetlib.Session
/*  37:    */ org.spacehq.packetlib.event.session.ConnectedEvent
/*  38:    */ org.spacehq.packetlib.event.session.DisconnectedEvent
/*  39:    */ org.spacehq.packetlib.event.session.PacketReceivedEvent
/*  40:    */ org.spacehq.packetlib.event.session.SessionAdapter
/*  41:    */ 
/*  42:    */ Frame
/*  43:    */   
/*  44:    */ 
/*  45: 41 */   instance = ()
/*  46: 43 */   console = ()
/*  47: 44 */   proxies = ()
/*  48: 45 */   accounts = ()
/*  49: 47 */   message = ()
/*  50:    */   serverIP
/*  51:    */   protocol
/*  52: 53 */   index = 0
/*  53:    */   shouldStop
/*  54:    */   msgCount
/*  55:    */   loop
/*  56:    */   
/*  57:    */   getInstance
/*  58:    */   
/*  59: 63 */     instance
/*  60:    */   
/*  61:    */   
/*  62:    */   setupFrame
/*  63:    */   
/*  64:    */     
/*  65:    */     
/*  66: 70 */       setLookAndFeelgetSystemLookAndFeelClassName()
/*  67:    */     
/*  68:    */      (
/*  69:    */     
/*  70: 74 */       printStackTrace()
/*  71:    */     
/*  72:    */      (
/*  73:    */     
/*  74: 78 */       printStackTrace()
/*  75:    */     
/*  76:    */      (
/*  77:    */     
/*  78: 82 */       printStackTrace()
/*  79:    */     
/*  80:    */      (
/*  81:    */     
/*  82: 86 */       printStackTrace()
/*  83:    */     
/*  84: 88 */     setTitle"ApocalypseBot | By Klintos"
/*  85: 89 */     setSize450, 650
/*  86: 90 */     setResizable
/*  87: 91 */     setLocationRelativeTo
/*  88: 92 */     getContentPane()setLayout
/*  89: 93 */     setDefaultCloseOperation3
/*  90:    */     
/*  91: 95 */      = "Accounts Loaded:"
/*  92: 96 */     setBounds7, 5, 150, 30
/*  93: 97 */     setFont"Calibri", 0, 20
/*  94: 98 */     getContentPane()add
/*  95:    */     
/*  96:100 */      = "Proxies Loaded:"
/*  97:101 */     setBounds7, 140, 150, 30
/*  98:102 */     setFont"Calibri", 0, 20
/*  99:103 */     add
/* 100:    */     
/* 101:105 */      = "0"
/* 102:    */     
/* 103:107 */      = 0, 0, 6000, 0
/* 104:108 */     addChangeListener()
/* 105:    */     
/* 106:    */       stateChanged
/* 107:    */       
/* 108:110 */         sliderLabelAmmountsetTextdelaySlidergetValue()
/* 109:    */       
/* 110:112 */     
/* 111:113 */     setBounds135, 423, 260, 22
/* 112:114 */     setFont"Calibri", 0, 20
/* 113:115 */     getContentPane()add
/* 114:    */     
/* 115:117 */     setBounds400, 420, 150, 30
/* 116:118 */     setFont"Calibri", 0, 20
/* 117:119 */     getContentPane()add
/* 118:    */     
/* 119:121 */      = "Throttle Delay:"
/* 120:122 */     setBounds7, 420, 150, 30
/* 121:123 */     setFont"Calibri", 0, 20
/* 122:124 */     getContentPane()add
/* 123:    */     
/* 124:126 */      = "Spam Message:"
/* 125:127 */     setBounds7, 366, 150, 30
/* 126:128 */     setFont"Calibri", 0, 20
/* 127:129 */     getContentPane()add
/* 128:    */     
/* 129:131 */      = "Use Proxies?"
/* 130:    */     
/* 131:133 */      = "Login All"
/* 132:134 */     addActionListener()
/* 133:    */     
/* 134:    */       actionPerformed
/* 135:    */       
/* 136:137 */         botArrayclear()
/* 137:138 */         shouldStop = 
/* 138:139 */         ()
/* 139:    */         
/* 140:    */           run
/* 141:    */           
/* 142:141 */              (val$proxyCheck.isSelected())
/* 143:    */             {
/* 144:142 */               if (Frame.index > Main.proxyArray.size()) {
/* 145:143 */                 Frame.index = 0;
/* 146:    */               }
/* 147:145 */               String[] proxySplit = ((String)Main.proxyArray.get(Frame.index)).split(":");
/* 148:146 */               String proxyIP = proxySplit[0];
/* 149:147 */               String proxyPort = proxySplit[1];
/* 150:148 */               System.setProperty("socksProxyHost", proxyIP);
/* 151:149 */               System.setProperty("socksProxyPort", proxyPort);
/* 152:    */             }
/* 153:151 */             String ip = "";
/* 154:    */             int port;
/* 155:    */             final int port;
/* 156:153 */             if (Frame.serverIP.getText().contains(":"))
/* 157:    */             {
/* 158:154 */               String[] serverSplit = Frame.serverIP.getText().split(":");
/* 159:155 */               ip = serverSplit[0];
/* 160:156 */               port = Integer.valueOf((serverSplit[1] == "") || (serverSplit[1] == null) ? "25565" : serverSplit[1]).intValue();
/* 161:    */             }
/* 162:    */             else
/* 163:    */             {
/* 164:158 */               ip = Frame.serverIP.getText();
/* 165:159 */               port = 25565;
/* 166:    */             }
/* 167:162 */             for (String string : Main.accountArray)
/* 168:    */             {
/* 169:163 */               if (Frame.shouldStop) {
/* 170:    */                 break;
/* 171:    */               }
/* 172:165 */               Frame.index += 1;
/* 173:    */               try
/* 174:    */               {
/* 175:167 */                 String[] accountSplit = string.split(":");
/* 176:    */                 
/* 177:169 */                 final Bot bot = new Bot(accountSplit[0], accountSplit[1], ip, port);
/* 178:170 */                 Main.botArray.add(bot);
/* 179:    */                 
/* 180:172 */                 bot.addListener(new SessionAdapter()
/* 181:    */                 {
/* 182:    */                   public void packetReceived(PacketReceivedEvent event)
/* 183:    */                   {
/* 184:174 */                     if ((event.getPacket() instanceof ServerChatPacket))
/* 185:    */                     {
/* 186:175 */                       ServerChatPacket chat = (ServerChatPacket)event.getPacket();
/* 187:176 */                       Frame.getInstance().printToConsole(chat.getMessage().getFullText());
/* 188:    */                     }
/* 189:    */                   }
/* 190:    */                   
/* 191:    */                   public void connected(ConnectedEvent event)
/* 192:    */                   {
/* 193:181 */                     Frame.getInstance().printToConsole("Connected bot " + bot.getUsername() + " to " + bot.getHost() + ":" + port);
/* 194:    */                   }
/* 195:    */                   
/* 196:    */                   public void disconnected(DisconnectedEvent event)
/* 197:    */                   {
/* 198:185 */                     Frame.getInstance().printToConsole("Disconnected: " + event.getReason());
/* 199:    */                   }
/* 200:187 */                 });
/* 201:188 */                 bot.getSession().connect(true);
/* 202:    */               }
/* 203:    */               catch (AuthenticationException e1)
/* 204:    */               {
/* 205:193 */                 Frame.getInstance().printToConsole(e1.getLocalizedMessage());
/* 206:    */               }
/* 207:    */               try
/* 208:    */               {
/* 209:196 */                 Thread.sleep(this.val$delaySlider.getValue());
/* 210:    */               }
/* 211:    */               catch (InterruptedException localInterruptedException) {}
/* 212:198 */               if (Frame.shouldStop) {
/* 213:    */                 break;
/* 214:    */               }
/* 215:    */             }
/* 216:    */           }
/* 217:    */         }.start();
/* 218:    */       }
/* 219:204 */     });
/* 220:205 */     start.setFont(new Font("Calibri", 0, 12));
/* 221:206 */     start.setBounds(1, 330, 220, 35);
/* 222:207 */     getContentPane().add(start);
/* 223:    */     
/* 224:209 */     JButton stop = new JButton("Disconnect All");
/* 225:210 */     stop.addActionListener(new ActionListener()
/* 226:    */     {
/* 227:    */       public void actionPerformed(ActionEvent e)
/* 228:    */       {
/* 229:213 */         for (Bot bot : Main.botArray) {
/* 230:214 */           if (bot.getSession().isConnected()) {
/* 231:215 */             bot.getSession().disconnect(bot.getUsername() + " stopped spamming.");
/* 232:    */           }
/* 233:    */         }
/* 234:217 */         Frame.shouldStop = true;
/* 235:    */       }
/* 236:219 */     });
/* 237:220 */     stop.setFont(new Font("Calibri", 0, 12));
/* 238:221 */     stop.setBounds(223, 330, 220, 35);
/* 239:222 */     getContentPane().add(stop);
/* 240:    */     
/* 241:224 */     JButton loadAlts = new JButton("Load Accounts");
/* 242:225 */     loadAlts.addActionListener(new ActionListener()
/* 243:    */     {
/* 244:    */       public void actionPerformed(ActionEvent e)
/* 245:    */       {
/* 246:228 */         Main.accountArray.clear();
/* 247:229 */         JFileChooser fileBrowser = new JFileChooser();
/* 248:230 */         int button = fileBrowser.showOpenDialog(Frame.this);
/* 249:231 */         if (button == 0) {
/* 250:232 */           FileUtils.loadFile(fileBrowser.getCurrentDirectory().toString() + "\\" + fileBrowser.getSelectedFile().getName(), Main.accountArray);
/* 251:    */         }
/* 252:234 */         for (String string : Main.accountArray) {
/* 253:235 */           Frame.this.accounts.addElement(string);
/* 254:    */         }
/* 255:    */       }
/* 256:238 */     });
/* 257:239 */     loadAlts.setFont(new Font("Calibri", 0, 12));
/* 258:240 */     loadAlts.setBounds(288, 34, 150, 102);
/* 259:241 */     getContentPane().add(loadAlts);
/* 260:    */     
/* 261:243 */     JButton loadProxies = new JButton("Load Proxies");
/* 262:244 */     loadProxies.addActionListener(new ActionListener()
/* 263:    */     {
/* 264:    */       public void actionPerformed(ActionEvent e)
/* 265:    */       {
/* 266:247 */         Main.proxyArray.clear();
/* 267:248 */         JFileChooser fileBrowser = new JFileChooser();
/* 268:249 */         int button = fileBrowser.showOpenDialog(Frame.this);
/* 269:250 */         if (button == 0) {
/* 270:251 */           FileUtils.loadFile(fileBrowser.getCurrentDirectory().toString() + "\\" + fileBrowser.getSelectedFile().getName(), Main.proxyArray);
/* 271:    */         }
/* 272:253 */         for (String string : Main.proxyArray) {
/* 273:254 */           Frame.this.proxies.addElement(string);
/* 274:    */         }
/* 275:    */       }
/* 276:257 */     });
/* 277:258 */     loadProxies.setFont(new Font("Calibri", 0, 12));
/* 278:259 */     loadProxies.setBounds(288, 169, 150, 80);
/* 279:260 */     getContentPane().add(loadProxies);
/* 280:    */     
/* 281:262 */     JScrollPane account = new JScrollPane();
/* 282:263 */     account.setBounds(2, 35, 280, 100);
/* 283:264 */     account.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
/* 284:    */     {
/* 285:    */       public void adjustmentValueChanged(AdjustmentEvent e)
/* 286:    */       {
/* 287:268 */         e.getAdjustable().setValue(e.getAdjustable().getMaximum());
/* 288:    */       }
/* 289:270 */     });
/* 290:271 */     getContentPane().add(account);
/* 291:272 */     final JList listAcc = new JList();
/* 292:273 */     listAcc.setModel(this.accounts);
/* 293:274 */     account.setViewportView(listAcc);
/* 294:    */     
/* 295:276 */     JScrollPane console = new JScrollPane();
/* 296:277 */     console.setBounds(2, 450, 440, 169);
/* 297:278 */     console.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
/* 298:    */     {
/* 299:    */       public void adjustmentValueChanged(AdjustmentEvent e)
/* 300:    */       {
/* 301:282 */         e.getAdjustable().setValue(e.getAdjustable().getMaximum());
/* 302:    */       }
/* 303:284 */     });
/* 304:285 */     getContentPane().add(console);
/* 305:286 */     JList list = new JList();
/* 306:287 */     list.setModel(this.console);
/* 307:288 */     console.setViewportView(list);
/* 308:    */     
/* 309:290 */     final JScrollPane proxy = new JScrollPane();
/* 310:291 */     proxy.setBounds(2, 170, 280, 100);
/* 311:292 */     proxy.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
/* 312:    */     {
/* 313:    */       public void adjustmentValueChanged(AdjustmentEvent e)
/* 314:    */       {
/* 315:296 */         e.getAdjustable().setValue(e.getAdjustable().getMaximum());
/* 316:    */       }
/* 317:298 */     });
/* 318:299 */     getContentPane().add(proxy);
/* 319:300 */     final JList listProxie = new JList();
/* 320:301 */     listProxie.setModel(this.proxies);
/* 321:302 */     proxy.setViewportView(listProxie);
/* 322:    */     
/* 323:304 */     proxyCheck.addActionListener(new ActionListener()
/* 324:    */     {
/* 325:    */       public void actionPerformed(ActionEvent e)
/* 326:    */       {
/* 327:306 */         if (proxyCheck.isEnabled()) {
/* 328:307 */           proxy.setViewportView(listProxie);
/* 329:    */         } else {
/* 330:309 */           proxy.setViewportView(listAcc);
/* 331:    */         }
/* 332:    */       }
/* 333:312 */     });
/* 334:313 */     proxyCheck.setFont(new Font("Calibri", 0, 12));
/* 335:314 */     proxyCheck.setBounds(286, 250, 150, 20);
/* 336:315 */     getContentPane().add(proxyCheck);
/* 337:    */     
/* 338:317 */     serverIP = new JTextField();
/* 339:318 */     serverIP.setBounds(90, 287, 280, 30);
/* 340:319 */     serverIP.setColumns(0);
/* 341:320 */     serverIP.addKeyListener(new KeyListener()
/* 342:    */     {
/* 343:    */       public void keyTyped(KeyEvent e) {}
/* 344:    */       
/* 345:    */       public void keyPressed(KeyEvent e) {}
/* 346:    */       
/* 347:    */       public void keyReleased(KeyEvent e) {}
/* 348:327 */     });
/* 349:328 */     getContentPane().add(serverIP);
/* 350:    */     
/* 351:330 */     JLabel serverLabel = new JLabel("Server IP:");
/* 352:331 */     serverLabel.setBounds(7, 288, 100, 30);
/* 353:332 */     serverLabel.setFont(new Font("Calibri", 0, 20));
/* 354:333 */     getContentPane().add(serverLabel);
/* 355:    */     
/* 356:335 */     message.setBounds(140, 370, 301, 22);
/* 357:336 */     message.setColumns(0);
/* 358:337 */     message.addKeyListener(new KeyListener()
/* 359:    */     {
/* 360:    */       public void keyTyped(KeyEvent e) {}
/* 361:    */       
/* 362:    */       public void keyPressed(KeyEvent e)
/* 363:    */       {
/* 364:343 */         if (e.getKeyCode() == 10)
/* 365:    */         {
/* 366:345 */           Frame.msgCount = 0;
/* 367:346 */           for (Bot bot : Main.botArray) {
/* 368:347 */             if (bot.getSession().isConnected())
/* 369:    */             {
/* 370:348 */               Frame.msgCount += 1;
/* 371:349 */               bot.getSession().send(new ClientChatPacket(Frame.message.getText()));
/* 372:    */             }
/* 373:    */           }
/* 374:352 */           Frame.message.setText("");
/* 375:353 */           Frame.this.printToConsole("Sent " + Frame.msgCount + " messages to the server.");
/* 376:    */         }
/* 377:    */       }
/* 378:    */       
/* 379:    */       public void keyReleased(KeyEvent e) {}
/* 380:358 */     });
/* 381:359 */     getContentPane().add(message);
/* 382:    */     
/* 383:361 */     JButton sendChat = new JButton("Send Mass Message");
/* 384:362 */     sendChat.addActionListener(new ActionListener()
/* 385:    */     {
/* 386:    */       public void actionPerformed(ActionEvent e)
/* 387:    */       {
/* 388:365 */         Frame.msgCount = 0;
/* 389:366 */         for (Bot bot : Main.botArray) {
/* 390:367 */           if (bot.getSession().isConnected())
/* 391:    */           {
/* 392:368 */             Frame.msgCount += 1;
/* 393:369 */             bot.getSession().send(new ClientChatPacket(Frame.message.getText()));
/* 394:    */           }
/* 395:    */         }
/* 396:372 */         Frame.message.setText("");
/* 397:373 */         Frame.this.printToConsole("Sent " + Frame.msgCount + " messages to the server.");
/* 398:    */       }
/* 399:375 */     });
/* 400:376 */     sendChat.setFont(new Font("Calibri", 0, 11));
/* 401:377 */     sendChat.setBounds(1, 395, 143, 25);
/* 402:378 */     getContentPane().add(sendChat);
/* 403:    */     
/* 404:380 */     JButton startLoop = new JButton("Start Looped Message");
/* 405:381 */     startLoop.addActionListener(new ActionListener()
/* 406:    */     {
/* 407:    */       public void actionPerformed(ActionEvent e)
/* 408:    */       {
/* 409:384 */         Frame.loop = true;
/* 410:385 */         new Thread()
/* 411:    */         {
/* 412:    */           public void run()
/* 413:    */           {
/* 414:387 */             while (Frame.loop) {
/* 415:    */               try
/* 416:    */               {
/* 417:389 */                 for (Bot bot : Main.botArray)
/* 418:    */                 {
/* 419:390 */                   if (bot.getSession().isConnected()) {
/* 420:391 */                     bot.getSession().send(new ClientChatPacket(Frame.message.getText()));
/* 421:    */                   }
/* 422:393 */                   Thread.sleep(this.val$delaySlider.getValue());
/* 423:    */                 }
/* 424:    */               }
/* 425:    */               catch (InterruptedException e1)
/* 426:    */               {
/* 427:396 */                 e1.printStackTrace();
/* 428:    */               }
/* 429:    */             }
/* 430:    */           }
/* 431:400 */         }.start();
/* 432:401 */         Frame.this.printToConsole("Started sending messages repeatedly.");
/* 433:    */       }
/* 434:403 */     });
/* 435:404 */     startLoop.setFont(new Font("Calibri", 0, 11));
/* 436:405 */     startLoop.setBounds(144, 395, 152, 25);
/* 437:406 */     getContentPane().add(startLoop);
/* 438:    */     
/* 439:408 */     JButton stopLoop = new JButton("Stop Looped Message");
/* 440:409 */     stopLoop.addActionListener(new ActionListener()
/* 441:    */     {
/* 442:    */       public void actionPerformed(ActionEvent e)
/* 443:    */       {
/* 444:412 */         Frame.loop = false;
/* 445:413 */         Frame.this.printToConsole("Stopped sending messages repeatedly.");
/* 446:    */       }
/* 447:415 */     });
/* 448:416 */     stopLoop.setFont(new Font("Calibri", 0, 11));
/* 449:417 */     stopLoop.setBounds(296, 395, 147, 25);
/* 450:418 */     getContentPane().add(stopLoop);
/* 451:    */     
/* 452:420 */     protocol = new JButton("1.7.2");
/* 453:421 */     protocol.addActionListener(new ActionListener()
/* 454:    */     {
/* 455:    */       public void actionPerformed(ActionEvent e)
/* 456:    */       {
/* 457:424 */         if (ProtocolConstants.PROTOCOL_VERSION == 5)
/* 458:    */         {
/* 459:425 */           Frame.this.printToConsole("Protocol Changed to 4.");
/* 460:426 */           ProtocolConstants.PROTOCOL_VERSION = 4;
/* 461:427 */           Frame.protocol.setText("1.7.2");
/* 462:    */         }
/* 463:428 */         else if (ProtocolConstants.PROTOCOL_VERSION == 4)
/* 464:    */         {
/* 465:429 */           Frame.this.printToConsole("Protocol Changed to 5.");
/* 466:430 */           ProtocolConstants.PROTOCOL_VERSION = 5;
/* 467:431 */           Frame.protocol.setText("1.7.9");
/* 468:    */         }
/* 469:    */       }
/* 470:434 */     });
/* 471:435 */     protocol.setFont(new Font("Calibri", 0, 11));
/* 472:436 */     protocol.setBounds(375, 286, 64, 31);
/* 473:437 */     getContentPane().add(protocol);
/* 474:    */     
/* 475:439 */     setVisible(true);
/* 476:    */   }
/* 477:    */   
/* 478:    */   public void printToConsole(String message)
/* 479:    */   {
/* 480:444 */     this.console.addElement(message);
/* 481:    */   }
/* 482:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.klintos.apocalypsebot.Frame
 * JD-Core Version:    0.7.0.1
 */
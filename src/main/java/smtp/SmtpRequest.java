
package smtp;

import smtp.command.Command;
import smtp.command.impl.ConnectCommand;
import smtp.command.impl.DataEndCommand;
import smtp.command.impl.EXPNCommand;
import smtp.command.impl.HelloCommand;
import smtp.command.impl.NoopCommand;
import smtp.command.impl.ReadInformationCommand;
import smtp.command.impl.ResetCommand;
import smtp.command.impl.UnknownCommand;

class SmtpRequest {
    private SmtpActionType action;
    private SmtpState state;
    String params;

    SmtpRequest(SmtpActionType actionType, String params, SmtpState state) {
        this.action = actionType;
        this.state = state;
        this.params = params;
    }

//    SmtpResponse execute() {
//        SmtpResponse response;
//        switch (action) {
//            case EXPN: {
//                response = new SmtpResponse(252, "Not supported", this.state);
//                break;
//            }
////            case SEND: {
////                response = new SmtpResponse(252, "Not supported", this.state);
////                break;
////            }
////            case SOML: {
////                response = new SmtpResponse(252, "Not supported", this.state);
////                break;
////            }
////            case SAML: {
////                response = new SmtpResponse(252, "Not supported", this.state);
////                break;
////            }
//            case HELP: {
//                response = new SmtpResponse(211, "No help available", this.state);
//                break;
//            }
//            case CONNECT: {
//                if (SmtpState.CONNECT == state) {
//                    response = new SmtpResponse(220, "localhost SMTP service ready", SmtpState.GREET);
//                } else {
//                    response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
//                }
//                break;
//            }
//            case NOOP: {
//                response = new SmtpResponse(250, "OK", this.state);
//                break;
//            }
//            case VRFY: {
//                response = new SmtpResponse(252, "Not supported", this.state);
//                break;
//            }
//            case RSET: {
//                response = new SmtpResponse(250, "OK", SmtpState.GREET);
//                break;
//            }
//            case EHLO: {
//                if (SmtpState.GREET == state) {
//                    response = new SmtpResponse(250, "OK", SmtpState.MAIL);
//                } else {
//                    response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
//                }
//                break;
//            }
//            case MAIL: {
//                if (SmtpState.MAIL == state || SmtpState.QUIT == state) {
//                    response = new SmtpResponse(250, "OK", SmtpState.RCPT);
//                } else {
//                    response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
//                }
//                break;
//            }
//            case RCPT: {
//                if (SmtpState.RCPT == state) {
//                    response = new SmtpResponse(250, "OK", this.state);
//                } else {
//                    response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
//                }
//                break;
//            }
//            case DATA: {
//                if (SmtpState.RCPT == state) {
//                    response = new SmtpResponse(354, "Start mail input; end with <CRLF>.<CRLF>", SmtpState.DATA_HDR);
//                } else {
//                    response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
//                }
//                break;
//            }
//            case BLANK_LINE: {
//                if (SmtpState.DATA_HDR == state) {
//                    response = new SmtpResponse(-1, "", SmtpState.DATA_BODY);
//                } else {
//                    if (SmtpState.DATA_BODY == state) {
//                        response = new SmtpResponse(-1, "", this.state);
//                    } else {
//                        response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
//                    }
//                }
//                break;
//            }
//            case QUIT: {
//                if (SmtpState.QUIT == state) {
//                    response = new SmtpResponse(221, "localhost service closing transmission channel", SmtpState.CONNECT);
//                } else {
//                    response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
//                }
//                break;
//            }
//            case DATA_END: {
//                if (SmtpState.DATA_HDR == state || SmtpState.DATA_BODY == state) {
//                    response = new SmtpResponse(250, "OK", SmtpState.QUIT);
//                } else {
//                    response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
//                }
//                break;
//            }
//            default: {
//                if (state != SmtpState.DATA_HDR && state != SmtpState.DATA_BODY) {
//                    response = new SmtpResponse(500, "Command not recognized " + action + " " + state, this.state);
//                } else {
//                    response = new SmtpResponse(-1, "", this.state);
//                }
//            }
//        }
//
//        return response;
//    }

    static Command findCommand(String line, SmtpState state, SmtpMail mail) {
        Command command = null;
        SmtpActionType action;
        String params = null;
        String request = line.toUpperCase();
        switch (state) {
            case DATA_HDR: {
                if (line.equals(".")) {
                    command = new DataEndCommand(SmtpActionType.DATA_END, state);
                    //action = SmtpActionType.DATA_END;
                } else {
                    if (line.length() < 1) {
                        command = new DataEndCommand(SmtpActionType.BLANK_LINE, state);
                        //  action = SmtpActionType.BLANK_LINE;
                    } else {
                        command = new UnknownCommand(state);
                        ///action = SmtpActionType.UNRECOG;
                        params = line;
                    }
                }
                break;
            }
            case DATA_BODY: {
                if (line.equals(".")) {
                    action = SmtpActionType.DATA_END;
                } else {
                    action = SmtpActionType.UNRECOG;
                    if (line.length() < 1) {
                        params = "\n";
                    } else {
                        params = line;
                    }
                }
                break;
            }
            default: {
                final int COMMAND_SIZE = 4;

                String commandName = request.substring(0, COMMAND_SIZE);
                switch (commandName) {
                    case "EHLO ": {
                        params = line.substring("EHLO ".length());
                        command = new HelloCommand(state, params);
                        break;
                    }
                    case "MAIL FROM:": {
                        action = SmtpActionType.MAIL;
                        params = line.substring("MAIL FROM:".length());
                        break;
                    }
                    case "RCPT TO:": {
                        action = SmtpActionType.RCPT;
                        params = line.substring("RCPT TO:".length());
                        break;
                    }
                    default: {

                        try {
                            action = SmtpActionType.valueOf(commandName);
                            switch (action) {
                                case EXPN: {
                                    command = new EXPNCommand(state);
                                    //    response = new SmtpResponse(252, "Not supported", this.state);
                                    break;
                                }
//            case SEND: {
//                response = new SmtpResponse(252, "Not supported", this.state);
//                break;
//            }
//            case SOML: {
//                response = new SmtpResponse(252, "Not supported", this.state);
//                break;
//            }
//            case SAML: {
//                response = new SmtpResponse(252, "Not supported", this.state);
//                break;
//            }
                                case HELP: {
                                    //  response = new SmtpResponse(211, "No help available", this.state);
                                    break;
                                }
                                case CONNECT: {
                                    command = new ConnectCommand(state);
                                    break;
                                }
                                case NOOP: {
                                    command = new NoopCommand(state);
                                    break;
                                }
                                case VRFY: {
                                    //  response = new SmtpResponse(252, "Not supported", this.state);
                                    break;
                                }
                                case RSET: {
                                    command = new ResetCommand(mail);
                                    // response = new SmtpResponse(250, "OK", SmtpState.GREET);
                                    break;
                                }
                                case MAIL: {
                                    if (SmtpState.MAIL == state || SmtpState.QUIT == state) {
//                                        response = new SmtpResponse(250, "OK", SmtpState.RCPT);
                                    } else {
//                                        response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
                                    }
                                    break;
                                }
                                case RCPT: {
                                    if (SmtpState.RCPT == state) {
//                                        response = new SmtpResponse(250, "OK", this.state);
                                    } else {
//                                        response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
                                    }
                                    break;
                                }
                                case DATA: {
                                    if (SmtpState.RCPT == state) {
//                                        response = new SmtpResponse(354, "Start mail input; end with <CRLF>.<CRLF>", SmtpState.DATA_HDR);
                                    } else {
//                                        response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
                                    }
                                    break;
                                }
                                case BLANK_LINE: {
                                    if (SmtpState.DATA_HDR == state) {
//                                        response = new SmtpResponse(-1, "", SmtpState.DATA_BODY);
                                    } else {
                                        if (SmtpState.DATA_BODY == state) {
//                                            response = new SmtpResponse(-1, "", this.state);
                                        } else {
//                                            response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
                                        }
                                    }
                                    break;
                                }
                                case QUIT: {
                                    if (SmtpState.QUIT == state) {
//                                        response = new SmtpResponse(221, "localhost service closing transmission channel", SmtpState.CONNECT);
                                    } else {
//                                        response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
                                    }
                                    break;
                                }
                                case DATA_END: {
                                    if (SmtpState.DATA_HDR == state || SmtpState.DATA_BODY == state) {
//                                        response = new SmtpResponse(250, "OK", SmtpState.QUIT);
                                    } else {
//                                        response = new SmtpResponse(503, "Bad sequence of commands: " + action, this.state);
                                    }
                                    break;
                                }
                                default: {
                                    if (state != SmtpState.DATA_HDR && state != SmtpState.DATA_BODY) {
//                                        response = new SmtpResponse(500, "Command not recognized " + action + " " + state, this.state);
                                    } else {
//                                        response = new SmtpResponse(-1, "", this.state);
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            if (state != SmtpState.DATA_HDR && state != SmtpState.DATA_BODY) {
                                command = new UnknownCommand(state);
                            } else {
                                command = new ReadInformationCommand(state);
                            }
                            action = SmtpActionType.UNRECOG;
                        }
                        break;
                    }

                }
            }
        }
        return command;
    }
}

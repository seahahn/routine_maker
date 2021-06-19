//package com.seahahn.routinemaker.sns.chat;
//
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.net.Socket;
//
//public class  Client {
//
//    private static final int PORT = 33333;
//    private static final String SERVER_ADDRESS = "15.165.168.238";
//    JFrame frame = new JFrame("Client");
//    JTextArea textArea = new JTextArea();
//    JTextField inputText = new JTextField();
//    Socket socket;
//    String host;
//    int port;
//    DataOutputStream outputStream;
//    DataInputStream inputStream;
//
//    public Client(String host, int port){
//        this.host = host;
//        this.port = port;
//        try {
//            // Socket 생성 및 접속
//            socket = throwSocket(host,port);
//
//            // 생성된 socket을 통해 입출력 스트림 생성
//            outputStream = connectOutputStream();
//            inputStream = connectInputStream();
//
//            sendChatRoomId("1");
//
//            showClientView();
//            while(true){
//                String msg = receiveMessageFromServer();
//                textArea.append(msg+"\n");
//            }
//        } catch (IOException ioException) {
//            ioException.printStackTrace();
//        }
//
//    }
//    public void showClientView (){
//        textArea.setEditable(false);
//        textArea.setText("클라이언트 화면\n");
//        frame.add("Center",textArea);
//        frame.add("South",inputText);
//        frame.setSize(300,500);
//        frame.setVisible(true);
//        eventHandler();
//    }
//    public void eventHandler(){
//        inputText.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    sendMessageToServer(inputText.getText());
//                } catch (IOException ioException) {
//                    ioException.printStackTrace();
//                }
//                inputText.setText("");
//            }
//
//        });
//    }
//
//    public Socket throwSocket(String host, int port) throws IOException {
//        socket = new Socket(host,port); // Socket 생성 및 접속
//
//        return socket;
//    }
//    public DataInputStream connectInputStream() throws IOException{
//        inputStream = new DataInputStream(socket.getInputStream()); // 생성된 socket을 통해 DataInputStream생성
//
//        return inputStream;
//    }
//    public DataOutputStream connectOutputStream() throws IOException{ // 생성된 socket을 통해 DataOutputStream생성
//        outputStream = new DataOutputStream(socket.getOutputStream());
//
//        return outputStream;
//    }
//    public void sendChatRoomId(String id) throws IOException {
//        outputStream.writeUTF(id); // 생성된 출력 스트림을 통하여 데이터 송신
//    }
//    public void sendMessageToServer(String msg) throws IOException {
//        outputStream.writeUTF(msg); // 생성된 출력 스트림을 통하여 데이터 송신
//    }
//    public String receiveMessageFromServer() throws IOException {
//        String msg = inputStream.readUTF(); // 생성된 입력 스트림을 통하여 데이터 수신
//
//        return msg;
//    }
//
//    public static void main(String[] args) {
//        Client client = new Client(SERVER_ADDRESS), PORT);
//    }
//}
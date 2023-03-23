package app.freerouting.gui;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import app.freerouting.interactive.BoardHandling;
import app.freerouting.board.RoutingBoard;
import app.freerouting.board.TestLevel;
import app.freerouting.interactive.Settings;
import app.freerouting.logger.FRLogger;

public class TcpSocket {

    public TcpSocket() {

    }


    public void Sendhandler(Socket p_socket, BoardHandling p_boardBoardHandling) throws IOException {

        byte[] data = SerializeHandler(p_boardBoardHandling);

        OutputStream os = p_socket.getOutputStream();

        os.write(data);

        os.flush();
    }

    public void SendBoard(Socket p_socket, RoutingBoard p_board) throws IOException {

        byte[] data = SerializeBoard(p_board);

        OutputStream os = p_socket.getOutputStream();

        os.write(data);

        os.flush();
    }

    public RoutingBoard ReceiveBoard(Socket p_socket) throws IOException{
        
        byte[] recvBuffer = new byte[9999*9999];

        InputStream is = p_socket.getInputStream();

        int nReadSize = is.read(recvBuffer);

        if (nReadSize > 0){
            return DeserializeBoard(recvBuffer);
        }

        return null;

    }

    public byte[] SerializeHandler(BoardHandling p_boardBoardHandling) {

        try {
            java.io.ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
            java.io.ObjectOutputStream object_stream = new java.io.ObjectOutputStream(output_stream);

            object_stream.writeObject(p_boardBoardHandling.board);
            object_stream.writeObject(p_boardBoardHandling.settings);
            object_stream.writeObject(p_boardBoardHandling.coordinate_transform);
            object_stream.writeObject(p_boardBoardHandling.graphics_context);

            object_stream.flush();
            object_stream.close();

            return output_stream.toByteArray();
        } catch (Exception e) {
            FRLogger.error("Couldn't serialize board", e);
        }

        return null;

    }

    public byte[] SerializeBoard(RoutingBoard p_board) {

        try {
            java.io.ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
            java.io.ObjectOutputStream object_stream = new java.io.ObjectOutputStream(output_stream);

            object_stream.writeObject(p_board);
            object_stream.flush();
            object_stream.close();

            return output_stream.toByteArray();
        } catch (Exception e) {
            FRLogger.error("Couldn't serialize board", e);
        }

        return null;

    }

    public RoutingBoard DeserializeBoard(byte[] p_bytearray) {

        if (p_bytearray != null) {
            try {
                java.io.ByteArrayInputStream input_stream = new ByteArrayInputStream(p_bytearray);
                java.io.ObjectInputStream object_stream = new java.io.ObjectInputStream(input_stream);


                RoutingBoard recvboard = (RoutingBoard) object_stream.readObject();

                recvboard.set_test_level(TestLevel.RELEASE_VERSION); // test_level is transient
          
                // board_copy.clear_autoroute_database();
                recvboard.clear_all_item_temporary_autoroute_data();
                recvboard.finish_autoroute();

                return recvboard;
            } catch (Exception e) {
                FRLogger.error("Couldn't deserialize board", e);
            }

        }
        return null;
    }

    public byte[] SerializeSettings(Settings p_Settings) {

        try {
            java.io.ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
            java.io.ObjectOutputStream object_stream = new java.io.ObjectOutputStream(output_stream);

            object_stream.writeObject(p_Settings);
            object_stream.close();

            return output_stream.toByteArray();
        } catch (Exception e) {
            FRLogger.error("Couldn't serialize Settings", e);
        }

        return null;

    }

    public Settings DeserializeSettings(byte[] p_bytearray) {

        if (p_bytearray != null) {
            try {
                java.io.ByteArrayInputStream input_stream = new ByteArrayInputStream(p_bytearray);
                java.io.ObjectInputStream object_stream = new java.io.ObjectInputStream(input_stream);

                return (Settings) object_stream.readObject();
            } catch (Exception e) {
                FRLogger.error("Couldn't deserialize Settings", e);
            }

        }
        return null;
    }
}

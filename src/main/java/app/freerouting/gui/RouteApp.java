package app.freerouting.gui;

import app.freerouting.board.TestLevel;
import app.freerouting.board.RoutingBoard;

import app.freerouting.interactive.InteractiveActionThread;
import app.freerouting.interactive.ThreadActionListener;
import app.freerouting.logger.FRLogger;

import java.io.ByteArrayInputStream;

import java.io.IOException;


import java.net.ServerSocket;
import java.net.Socket;

public class RouteApp {
  /**
   * Main function of the Application
   *
   * @param args
   */

  public static void main(String[] args) {

    FRLogger.traceEntry("RouteApp.main()");

    Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
    StartupOptions startupOptions = StartupOptions.parse(args);

      BoardFrame.Option board_option;
      if (startupOptions.session_file_option) {
        board_option = BoardFrame.Option.SESSION_FILE;
      } else {
        board_option = BoardFrame.Option.SINGLE_FRAME;
      }

      TcpSocket p_tcpSocket = new TcpSocket();
      RoutingBoard sendedBoard = null;
      DesignFile recvdesign_file = DesignFile.get_instance(null, false);
      final BoardFrame recv_frame = new BoardFrame(
          recvdesign_file,
          board_option,
          TestLevel.RELEASE_VERSION,
          startupOptions.current_locale,
          !startupOptions.test_version_option,
          startupOptions.save_intermediate_stages,
          startupOptions.optimization_improvement_threshold);
          
      try {
        int port = 1120;

        ServerSocket server = new ServerSocket(port);

        while (true) {
          Socket socket = server.accept();
          FRLogger.info("request from " + socket.getInetAddress());
          
          byte[] recvBuffer = new byte[9999*9999];
          java.io.InputStream is = socket.getInputStream();
          int nReadSize = is.read(recvBuffer);
          if (nReadSize > 0){
            java.io.ByteArrayInputStream input_stream = new ByteArrayInputStream(recvBuffer);
            java.io.ObjectInputStream object_stream = new java.io.ObjectInputStream(input_stream);
            recv_frame.board_panel.board_handling.read_design(object_stream, TestLevel.RELEASE_VERSION);
          }

          if (recv_frame.board_panel.board_handling.get_routing_board() != null) {      

            FRLogger.traceEntry("Batch Auto Route");
            InteractiveActionThread thread = recv_frame.board_panel.board_handling.start_batch_autorouter();

            thread.addListener(
                new ThreadActionListener() {
                  @Override
                  public void autorouterStarted() {
                  }

                  @Override
                  public void autorouterAborted() {
                    SendRoutedBoard();
                  }

                  @Override
                  public void autorouterFinished() {
                    SendRoutedBoard();
                  }

                  private void SendRoutedBoard() {
                    try {
                      FRLogger.traceExit("Batch Auto Route");

                      p_tcpSocket.SendBoard(socket, recv_frame.board_panel.board_handling.board);
                      socket.close();
                    } catch (IOException e) {
                      e.printStackTrace();
                    }

                  }
                });
          }

        }

      } catch (IOException e) {
        e.printStackTrace();
      }


    FRLogger.traceExit("RouteApp.main()");
  }

}
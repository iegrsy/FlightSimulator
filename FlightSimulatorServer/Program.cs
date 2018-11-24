using System;
using System.Drawing;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using FlightSimulator;
using Grpc.Core;
using Helper;

namespace FlightSimulatorServer
{
    public class FlightSimulatorSeviceImpl : FlightSimulatorService.FlightSimulatorServiceBase
    {
        public FlightSimulatorSeviceImpl() { }

        public override async Task GetCameraStream(DummyQ request, IServerStreamWriter<CameraStreamQ> responseStream, ServerCallContext context)
        {
            Console.WriteLine("Connect host: " + context.Host + " \nmethod: " + context.Method);

            SampleImage sampleImage = new SampleImage(400, 300);

            int count = 0;
            while (true)
            {
                if (count > 100)
                    break;

                byte[] img = new byte[] { (byte)0x88 };
                img = sampleImage.getSample();

                Console.WriteLine(DateTime.Now.ToString(SampleImage.timeformat) + ": response img data size: " + img.Length);

                await responseStream.WriteAsync(new CameraStreamQ()
                {
                    Streams = { new CameraStream() { Data = Google.Protobuf.ByteString.CopyFrom(img) } }
                });

                count++;
            }
        }
    }

    class Program
    {
        static void Main(string[] args)
        {
            string _host = "192.168.1.20";
            int _port = 8888;

            // Server server = new Server
            // {
            //     Services = { FlightSimulatorService.BindService(new FlightSimulatorSeviceImpl()) },
            //     Ports = { new ServerPort(_host, _port, ServerCredentials.Insecure) }
            // };
            // server.Start();

            Console.WriteLine("Server listening on port " + _port);
            Console.WriteLine("Press any key to stop the server...");

            sendUDP();

            Console.ReadKey();

            // server.ShutdownAsync().Wait();
        }

        static void sendUDP()
        {
            Socket sock = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
            IPAddress serverAddr = IPAddress.Parse("198.168.1.21");
            IPEndPoint endPoint = new IPEndPoint(serverAddr, 9999);

            byte[] send_buffer = new byte[] { (byte)0x89 };
            SampleImage image = new SampleImage(100, 100);

            for (int i = 0; i < 1000; i++)
            {
                send_buffer = image.getSample();
                sock.SendTo(send_buffer, endPoint);
                System.Threading.Thread.Sleep(500);
                Console.WriteLine("Waiting.... " + i);
            }

            sock.Close();
        }
    }
}

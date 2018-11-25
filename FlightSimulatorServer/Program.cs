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
            string _host = "127.0.0.1";
            int _portRPC = 8888;
            int _portUDP = 9999;

            Server server = new Server
            {
                Services = { FlightSimulatorService.BindService(new FlightSimulatorSeviceImpl()) },
                Ports = { new ServerPort(_host, _portRPC, ServerCredentials.Insecure) }
            };
            server.Start();

            UdpServer udpServer = new UdpServer();
            udpServer.StartUDPListener(_portUDP);

            Console.WriteLine("Server listening on IP: " + _host);
            Console.WriteLine("Server listening on RPC port: " + _portRPC);
            Console.WriteLine("Server listening on UDP port: " + _portUDP);
            Console.WriteLine("Press any key to stop the server...");

            Console.ReadKey();

            server.ShutdownAsync().Wait();
        }
    }

    class UdpServer
    {
        private int port;
        private UdpClient client;

        public void StartUDPListener(int _port)
        {
            port = _port;
            client = new UdpClient(port);
            try
            {
                client.BeginReceive(new AsyncCallback(ListenForData), null);
            }
            catch (Exception e)
            {
                Console.WriteLine("On client connect exception " + e);
            }
        }

        private void ListenForData(IAsyncResult result)
        {
            IPEndPoint endPoint = new IPEndPoint(IPAddress.Any, port);
            byte[] received = client.EndReceive(result, ref endPoint);

            String sensorValues = Encoding.ASCII.GetString(received);
            System.Console.WriteLine("Sensor: " + sensorValues);
            client.BeginReceive(new AsyncCallback(ListenForData), null);
        }
    }
}

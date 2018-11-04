using System;
using System.Drawing;
using System.IO;
using System.Threading.Tasks;
using FlightSimulator;
using Grpc.Core;

namespace FlightSimulatorServer
{
    public class FlightSimulatorSeviceImpl : FlightSimulatorService.FlightSimulatorServiceBase
    {
        static string timeformat = "dd/MM/yyyy HH:mm:ss.fff";

        public FlightSimulatorSeviceImpl() { }

        public static byte[] ImageToByteArray(System.Drawing.Image imageIn)
        {
            using (var ms = new MemoryStream())
            {
                imageIn.Save(ms, System.Drawing.Imaging.ImageFormat.Bmp);
                return ms.ToArray();
            }
        }

        public override async Task GetCameraStream(DummyQ request, IServerStreamWriter<CameraStreamQ> responseStream, ServerCallContext context)
        {
            Console.WriteLine("Connect host: " + context.Host + " \nmethod: " + context.Method);

            Image image = new Bitmap(400, 300);
            Graphics graph = Graphics.FromImage(image);
            Pen pen = new Pen(Brushes.Black);

            int count = 0;
            while (true)
            {
                if (count > 100)
                    break;

                byte[] img = new byte[] { (byte)0x88 };
                try
                {
                    graph.Clear(Color.Black);
                    graph.DrawString(
                        DateTime.Now.ToString(timeformat),
                        new Font(new FontFamily("DecoType Thuluth"), 12, FontStyle.Bold | FontStyle.Underline),
                        Brushes.Red,
                        new PointF(0, image.Height / 2)
                    );

                    img = ImageToByteArray(image);
                }
                catch (Exception e) { Console.WriteLine(e.ToString()); }

                Console.WriteLine(DateTime.Now.ToString(timeformat) + ": response img data size: " + img.Length);

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
            string _host = "192.168.1.30";
            int _port = 8888;

            Server server = new Server
            {
                Services = { FlightSimulatorService.BindService(new FlightSimulatorSeviceImpl()) },
                Ports = { new ServerPort(_host, _port, ServerCredentials.Insecure) }
            };
            server.Start();

            Console.WriteLine("Server listening on port " + _port);
            Console.WriteLine("Press any key to stop the server...");
            Console.ReadKey();

            server.ShutdownAsync().Wait();
        }
    }
}

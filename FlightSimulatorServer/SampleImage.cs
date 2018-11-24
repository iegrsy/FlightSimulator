using System;
using System.Drawing;
using System.IO;

namespace Helper
{
    class SampleImage
    {
        public static string timeformat = "dd/MM/yyyy HH:mm:ss.fff";

        private Image image;
        private Graphics graph;
        private Pen pen;

        public SampleImage(int w, int h)
        {
            image = new Bitmap(w, h);
            graph = Graphics.FromImage(image);
            pen = new Pen(Brushes.Black);
        }

        public byte[] getSample()
        {
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

            return img;
        }

        public static byte[] ImageToByteArray(System.Drawing.Image imageIn)
        {
            using (var ms = new MemoryStream())
            {
                imageIn.Save(ms, System.Drawing.Imaging.ImageFormat.Bmp);
                return ms.ToArray();
            }
        }
    }
}
using System;
using System.Collections;
using System.Collections.Generic;
using System.Net.Sockets;
using System.Net;
using System.Text;
using UnityEngine;

public class ControlScript : MonoBehaviour
{
    private string request;

    private int port;
    private UdpClient client;

    private void StartUDPListener(int _port)
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
        valueChange(sensorValues);

        client.BeginReceive(new AsyncCallback(ListenForData), null);
    }

    // Use this for initialization
    void Start()
    {
        StartUDPListener(9999);
    }

    void valueChange(string rest)
    {
        if (rest.Equals(request))
            return;

        request = rest;
        if (request.StartsWith("head?"))
            SetCamera(request);
        else if (request.StartsWith("joystick?"))
            SetVehicle(request);
        else
            Debug.Log("Unknow request.");
    }

    void SetVehicle(string str)
    {
        float[] vector;
        try
        {
            vector = GetVector(str); // yaw, pitch, roll
        }
        catch (Exception e) { Debug.Log(e.Message); }
    }

    void SetCamera(string str)
    {
        float[] vector;
        try
        {
            vector = GetVector(str); // yaw, pitch, roll
        }
        catch (Exception e) { Debug.Log(e.Message); }
    }

    // Update is called once per frame
    void Update()
    {

    }

    public static Quaternion Euler(float yaw, float pitch, float roll)
    {
        yaw *= Mathf.Deg2Rad;
        pitch *= Mathf.Deg2Rad;
        roll *= Mathf.Deg2Rad;

        double yawOver2 = yaw * 0.5f;
        float cosYawOver2 = (float)System.Math.Cos(yawOver2);
        float sinYawOver2 = (float)System.Math.Sin(yawOver2);
        double pitchOver2 = pitch * 0.5f;
        float cosPitchOver2 = (float)System.Math.Cos(pitchOver2);
        float sinPitchOver2 = (float)System.Math.Sin(pitchOver2);
        double rollOver2 = roll * 0.5f;
        float cosRollOver2 = (float)System.Math.Cos(rollOver2);
        float sinRollOver2 = (float)System.Math.Sin(rollOver2);
        Quaternion result;
        result.w = cosYawOver2 * cosPitchOver2 * cosRollOver2 + sinYawOver2 * sinPitchOver2 * sinRollOver2;
        result.x = sinYawOver2 * cosPitchOver2 * cosRollOver2 + cosYawOver2 * sinPitchOver2 * sinRollOver2;
        result.y = cosYawOver2 * sinPitchOver2 * cosRollOver2 - sinYawOver2 * cosPitchOver2 * sinRollOver2;
        result.z = cosYawOver2 * cosPitchOver2 * sinRollOver2 - sinYawOver2 * sinPitchOver2 * cosRollOver2;

        return result;
    }

    public static float[] GetVector(string str)
    {
        string[] p = str.Split('?');
        if (p.Length != 2)
            throw new Exception("Wrong type request:" + str);

        string[] p1 = p[1].Split(',');
        if (p1.Length != 3)
            throw new Exception("Wrong type request:" + p[1]);

        float[] s = new float[3];
        s[0] = float.Parse(p1[0]);
        s[1] = float.Parse(p1[1]);
        s[2] = float.Parse(p1[2]);

        return s;
    }
}

using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class turn : MonoBehaviour
{

    // Use this for initialization
    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {
        transform.RotateAroundLocal(Vector3.forward, 20 * Time.deltaTime);
    }
}

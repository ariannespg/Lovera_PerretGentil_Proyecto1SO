/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Estructuras;

import Modelo.Proceso;

/**
 *
 * @author adrianlovera
 */
public class ListaEnlazada {
    private Nodo head; // Primer elemento de la lista
    private Nodo tail; // Último elemento de la lista

    private static class Nodo {
        Proceso proceso;
        Nodo siguiente;

        Nodo(Proceso proceso) {
            this.proceso = proceso;
            this.siguiente = null;
        }
    }

    // Agregar proceso al final de la lista (simulando una cola FIFO)
    public void agregar(Proceso proceso) {
        Nodo nuevoNodo = new Nodo(proceso);
        if (tail == null) {
            head = tail = nuevoNodo;
        } else {
            tail.siguiente = nuevoNodo;
            tail = nuevoNodo;
        }
    }

    // Remover y retornar el primer proceso en la lista
    public Proceso remover() {
        if (head == null) return null;
        Proceso proceso = head.proceso;
        head = head.siguiente;
        if (head == null) tail = null;
        return proceso;
    }

    // Verificar si la lista está vacía
    public boolean estaVacia() {
        return head == null;
    }

    // Obtener el primer proceso sin removerlo
    public Proceso obtenerPrimero() {
        return head != null ? head.proceso : null;
    }

    // Obtener el proceso con menor cantidad de instrucciones (para SJF)
    public Proceso obtenerSJF() {
        if (head == null) return null;

        Nodo menorNodo = head;
        Nodo actual = head.siguiente;
        Nodo previoMenor = null;
        Nodo previo = head;

        while (actual != null) {
            if (actual.proceso.getPcb().getProgramCounter() < menorNodo.proceso.getPcb().getProgramCounter()) {
                menorNodo = actual;
                previoMenor = previo;
            }
            previo = actual;
            actual = actual.siguiente;
        }

        // Si el menor proceso no es el primero, ajustar enlaces
        if (menorNodo != head) {
            previoMenor.siguiente = menorNodo.siguiente;
            if (menorNodo == tail) tail = previoMenor;
            menorNodo.siguiente = head;
            head = menorNodo;
        }

        return remover();
    }
    
    // Método en ListaEnlazada para obtener los procesos en un array de Strings
public String[] obtenerListaProcesos() {
    Nodo actual = head;
    int size = 0;
    
    // Contar elementos
    while (actual != null) {
        size++;
        actual = actual.siguiente;
    }

    // Crear array y llenarlo con los nombres de los procesos
    String[] lista = new String[size];
    actual = head;
    int i = 0;
    while (actual != null) {
        lista[i++] = actual.proceso.getNombre() + " - " + actual.proceso.getEstado();
        actual = actual.siguiente;
    }
    return lista;
}


}

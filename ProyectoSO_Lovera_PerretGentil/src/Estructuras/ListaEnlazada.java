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

/**
 * Estructura de datos tipo lista enlazada simple 
 * para manejar los procesos.
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

    // Obtener el proceso con menor cantidad de instrucciones restantes (para SJF)
    // Se asume que "tiempo restante" ~ (instrucciones totales - PC).
    public Proceso obtenerSJF() {
    if (head == null) return null; // 📌 Lista vacía

    Nodo menorNodo = head;
    Nodo actual = head.siguiente;
    Nodo previoMenor = null;
    Nodo previo = head;

    int tiempoMenor = menorNodo.proceso.getInstrucciones() - menorNodo.proceso.getPC();

    while (actual != null) {
        int tiempoActual = actual.proceso.getInstrucciones() - actual.proceso.getPC();
        if (tiempoActual < tiempoMenor) {
            menorNodo = actual;
            previoMenor = previo;
            tiempoMenor = tiempoActual;
        }
        previo = actual;
        actual = actual.siguiente;
    }

    // 📌 Si el proceso más corto está en `head`, eliminarlo con `remover()`
    if (menorNodo == head) {
        return remover(); // ✅ Remueve y retorna el primer nodo
    }

    // 📌 Si el proceso más corto está en el medio de la lista
    if (previoMenor != null) {
        previoMenor.siguiente = menorNodo.siguiente;
    }

    // 📌 Si el proceso más corto era el último, actualizar `tail`
    if (menorNodo == tail) {
        tail = previoMenor;
    }

    Proceso procesoSeleccionado = menorNodo.proceso;

    // 📌 Asegurar que el nodo eliminado no siga apuntando a la lista
    menorNodo.siguiente = null;

    return procesoSeleccionado; // ✅ Ahora se elimina correctamente de `colaListos`
}
    
    // Retorna un arreglo con los nombres y estados de los procesos (para mostrar en GUI)
    public String[] obtenerListaProcesos() {
        Nodo actual = head;
        int size = 0;
        
        // Contar elementos
        while (actual != null) {
            size++;
            actual = actual.siguiente;
        }

        String[] lista = new String[size];
        actual = head;
        int i = 0;
        while (actual != null) {
            lista[i++] = actual.proceso.getNombre() + " - " + actual.proceso.getEstado();
            actual = actual.siguiente;
        }
        return lista;
    }

    // Retorna un arreglo con todos los procesos
    public Proceso[] obtenerTodosProcesos() {
        Nodo actual = head;
        int size = 0;
        while (actual != null) {
            size++;
            actual = actual.siguiente;
        }

        Proceso[] lista = new Proceso[size];
        actual = head;
        int i = 0;
        while (actual != null) {
            lista[i++] = actual.proceso;
            actual = actual.siguiente;
        }
        return lista;
    }

    // Remueve un proceso específico
    public boolean removerProceso(Proceso proceso) {
        if (head == null) return false; // Lista vacía

        // Si el proceso a eliminar es el primero
        if (head.proceso.equals(proceso)) {
            head = head.siguiente;
            if (head == null) tail = null;
            return true;
        }

        // Buscar el proceso en la lista
        Nodo actual = head;
        while (actual.siguiente != null) {
            if (actual.siguiente.proceso.equals(proceso)) {
                actual.siguiente = actual.siguiente.siguiente;
                if (actual.siguiente == null) tail = actual; 
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }
}
package org.madrona.http.client;

/**
 * Callback which can be passed some object which cannot be computed on the
 * calling thread.
 * <p/>
 * Typically the object which is passed to a Receiver is not meant to be touched
 * except from within the receive() method; implementations may proxy an
 * interface to one which will check that the correct locks are held and throw
 * an exception otherwise.
 *
 * @author Tim Boudreau
 */
public abstract class Receiver<T> {
    public abstract void receive(T object);

    public <E extends Throwable> void onFail(E exception) throws E {
        throw exception;
    }

    public void onFail() {
        //do nothing
        System.err.println(this + " failed");
    }
}